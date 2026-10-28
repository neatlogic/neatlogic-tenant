/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package neatlogic.module.tenant.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.exception.file.EmptyFileException;
import neatlogic.framework.exception.file.FileExtNotAllowedException;
import neatlogic.framework.exception.file.FileTooLargeException;
import neatlogic.framework.util.$;
import neatlogic.module.tenant.exception.file.NetworkImageDownloadException;
import neatlogic.module.tenant.exception.file.NetworkImageEncodingException;
import neatlogic.module.tenant.exception.file.NetworkImageFormatMismatchException;
import neatlogic.module.tenant.exception.file.NetworkImageHttpStatusException;
import neatlogic.module.tenant.exception.file.NetworkImageRedirectException;
import neatlogic.module.tenant.exception.file.NetworkImageUrlEmptyException;
import neatlogic.module.tenant.exception.file.NetworkImageUrlFormatException;
import neatlogic.module.tenant.exception.file.NetworkImageUrlIllegalException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

/** 下载网络图片到临时文件，统一处理地址格式、附件限制和资源释放。 */
public class NetworkImageDownloader {
    private static final Logger logger = LoggerFactory.getLogger(NetworkImageDownloader.class);
    private static final int MAX_REDIRECTS = 5;

    /** 下载时统计实际字节数，响应未声明长度时仍可执行大小限制。 */
    public DownloadedImage download(String url, JSONObject config) throws Exception {
        URI current = validateUri(url);
        Path temporaryFile = null;
        Path sanitizedFile = null;
        boolean ownershipTransferred = false;
        long maxSize = config == null ? 0 : config.getLongValue("maxSize");
        try {
            DownloadedImage result;
            try (CloseableHttpClient client = createHttpClient()) {
                for (int redirectCount = 0; ; redirectCount++) {
                    HttpGet request = new HttpGet(current);
                    request.setHeader("Accept", "image/*");
                    request.setHeader("Accept-Encoding", "identity");
                    try (CloseableHttpResponse response = client.execute(request)) {
                        int status = response.getStatusLine().getStatusCode();
                        if (isRedirect(status)) {
                            Header location = response.getFirstHeader("Location");
                            if (redirectCount >= MAX_REDIRECTS || location == null || StringUtils.isBlank(location.getValue())) {
                                // 使用专属异常标识重定向失败，保留既有触发条件。
                                throw new NetworkImageRedirectException();
                            }
                            // 每一跳重新校验地址格式，确保重定向仍使用 HTTP/HTTPS。
                            current = validateUri(current.resolve(location.getValue()).toString());
                            continue;
                        }
                        if (status != 200) {
                            // 状态码交给专属异常作为文案参数，避免在下载器中拼接错误信息。
                            throw new NetworkImageHttpStatusException(status);
                        }
                        HttpEntity entity = response.getEntity();
                        Header typeHeader = response.getFirstHeader("Content-Type");
                        // 声明类型仅用于选择 JPEG/PNG/GIF 校验器，随后仍需校验实际图片内容。
                        String contentType = NetworkImageSanitizer.normalizeContentType(typeHeader == null ? null : typeHeader.getValue());
                        if (entity == null) {
                            throw new EmptyFileException();
                        }
                        String name = resolveFileName(current, contentType);
                        validateExtension(name, config);
                        if (maxSize > 0 && entity.getContentLength() > maxSize) {
                            throw new FileTooLargeException(entity.getContentLength(), maxSize);
                        }
                        temporaryFile = createTemporaryFile();
                        long size = 0;
                        try (InputStream input = entity.getContent(); OutputStream output = Files.newOutputStream(temporaryFile)) {
                            byte[] buffer = new byte[8192];
                            int length;
                            while ((length = input.read(buffer)) != -1) {
                                size += length;
                                if (maxSize > 0 && size > maxSize) {
                                    throw new FileTooLargeException(size, maxSize);
                                }
                                output.write(buffer, 0, length);
                            }
                        }
                        if (size == 0) {
                            throw new EmptyFileException();
                        }
                        // 原始网络数据不得进入附件存储，仅保存完整解码后重新编码的标准图片。
                        sanitizedFile = createTemporaryFile();
                        createSanitizer().sanitize(temporaryFile, sanitizedFile, contentType);
                        long sanitizedSize = Files.size(sanitizedFile);
                        // 编码器即使未抛异常，也不能将空输出作为有效附件交给持久化流程。
                        if (sanitizedSize == 0) {
                            throw new NetworkImageEncodingException(new IOException("Image encoder produced an empty file"));
                        }
                        if (maxSize > 0 && sanitizedSize > maxSize) {
                            throw new FileTooLargeException(sanitizedSize, maxSize);
                        }
                        result = new DownloadedImage(sanitizedFile, name, contentType, sanitizedSize);
                    }
                    break;
                }
            }
            // 响应和客户端都关闭成功后才移交文件，避免 close 异常导致临时文件泄漏。
            ownershipTransferred = true;
            return result;
        } catch (IOException ex) {
            // 保留原始输入输出异常，专属异常负责格式化下载失败文案。
            throw new NetworkImageDownloadException(ex);
        } finally {
            // 原始文件始终删除；重新编码文件仅在成功返回后由 DownloadedImage 管理。
            deleteTemporaryFile(temporaryFile);
            if (!ownershipTransferred) {
                deleteTemporaryFile(sanitizedFile);
            }
        }
    }

    /** 创建独立的图片内容校验与重新编码器，便于验证编码失败路径。 */
    protected NetworkImageSanitizer createSanitizer() {
        return new NetworkImageSanitizer();
    }

    /** 使用 HttpClient 默认 DNS 解析与 TLS 证书校验，仅设置下载超时和请求行为。 */
    protected CloseableHttpClient createHttpClient() {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(10000)
                .setConnectionRequestTimeout(10000).setSocketTimeout(30000).build();
        return HttpClients.custom().setDefaultRequestConfig(config).disableRedirectHandling().disableAutomaticRetries()
                .disableCookieManagement().disableAuthCaching().disableContentCompression().build();
    }

    /** 下载流及编码输出写入系统临时文件；像素解码另由校验器按尺寸上限分配内存。 */
    protected Path createTemporaryFile() throws IOException {
        return Files.createTempFile("neatlogic-network-image-", ".tmp");
    }

    /** 仅校验地址格式与 HTTP/HTTPS 协议，原样保留查询参数并移除 fragment。 */
    public static URI validateUri(String url) {
        try {
            if (StringUtils.isBlank(url)) {
                // 空地址单独区分，不改变原有地址校验顺序。
                throw new NetworkImageUrlEmptyException();
            }
            URI uri = new URI(url.trim());
            String scheme = uri.getScheme();
            if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    || StringUtils.isBlank(uri.getHost()) || uri.getRawUserInfo() != null
                    || uri.getPort() == 0 || uri.getPort() > 65535) {
                // 保留协议、主机、凭据和端口限制，统一由地址非法异常描述。
                throw new NetworkImageUrlIllegalException();
            }
            String raw = uri.toString();
            int fragment = raw.indexOf('#');
            return fragment < 0 ? uri : new URI(raw.substring(0, fragment));
        } catch (URISyntaxException | IllegalArgumentException ex) {
            // 解析失败保留 cause，便于定位 URI 格式问题。
            throw new NetworkImageUrlFormatException(ex);
        }
    }

    /** 仅手动处理明确支持的重定向状态码。 */
    private boolean isRedirect(int status) {
        return status == 301 || status == 302 || status == 303 || status == 307 || status == 308;
    }

    /** 从最终地址获取安全文件名，无扩展名时根据图片 MIME 补全。 */
    private String resolveFileName(URI uri, String contentType) {
        String path = uri.getPath();
        String name = path == null ? "" : path.substring(path.lastIndexOf('/') + 1);
        name = name.replaceAll("[\\p{Cntrl}\\\\/:*?\"<>|]", "_").replaceAll("^[. ]+|[. ]+$", "");
        if (StringUtils.isBlank(name)) {
            name = "image_" + UUID.randomUUID().toString().replace("-", "");
        }
        if (name.length() > 180) {
            name = name.substring(0, 180);
        }
        int extensionIndex = name.lastIndexOf('.');
        if (extensionIndex <= 0) {
            name += "image/jpeg".equals(contentType) ? ".jpg" : "image/gif".equals(contentType) ? ".gif" : ".png";
        } else {
            // 已有扩展名必须与规范化格式相符，不能通过改名掩盖声明不一致。
            String extension = name.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
            boolean matches = "image/jpeg".equals(contentType)
                    ? "jpg".equals(extension) || "jpeg".equals(extension)
                    : "image/gif".equals(contentType) ? "gif".equals(extension) : "png".equals(extension);
            if (!matches) {
                throw new NetworkImageFormatMismatchException();
            }
        }
        return name;
    }

    /** 保持通用附件上传的白名单优先、其次黑名单规则。 */
    private void validateExtension(String name, JSONObject config) {
        if (config == null) {
            return;
        }
        String extension = name.substring(name.lastIndexOf('.') + 1);
        JSONArray whiteList = config.getJSONArray("whiteList");
        JSONArray blackList = config.getJSONArray("blackList");
        if (whiteList != null && !whiteList.isEmpty()) {
            if (!containsExtension(whiteList, extension)) {
                throw new FileExtNotAllowedException(extension);
            }
        } else if (blackList != null && containsExtension(blackList, extension)) {
            throw new FileExtNotAllowedException(extension);
        }
    }

    /** 扩展名比较忽略大小写，与已有上传接口一致。 */
    private boolean containsExtension(JSONArray extensions, String extension) {
        for (int i = 0; i < extensions.size(); i++) {
            if (extension.equalsIgnoreCase(extensions.getString(i))) {
                return true;
            }
        }
        return false;
    }

    /** 清理失败只记日志，不覆盖下载异常或已成功入库的结果。 */
    private static void deleteTemporaryFile(Path file) {
        if (file != null) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ex) {
                logger.error($.t("nmtu.networkimagedownloader.deletetemporaryfile"), file, ex);
            }
        }
    }

    /** 临时下载结果同时持有文件生命周期，调用方使用后自动删除。 */
    public static class DownloadedImage implements AutoCloseable {
        private final Path file;
        private final String name;
        private final String contentType;
        private final long size;

        /** 记录完整解码、重新编码且通过附件规则校验的最终临时图片及其大小。 */
        public DownloadedImage(Path file, String name, String contentType, long size) {
            this.file = file;
            this.name = name;
            this.contentType = contentType;
            this.size = size;
        }

        /** 返回用于传递到文件存储的临时路径。 */
        public Path getFile() { return file; }

        /** 返回已清理并补全扩展名的附件名称。 */
        public String getName() { return name; }

        /** 返回不含附加参数的图片 MIME 类型。 */
        public String getContentType() { return contentType; }

        /** 返回重新编码后最终文件的字节数，供附件存储和 FileVo.size 使用。 */
        public long getSize() { return size; }

        /** 使用完成后删除临时文件，清理异常不改变接口业务结果。 */
        @Override
        public void close() { deleteTemporaryFile(file); }
    }
}
