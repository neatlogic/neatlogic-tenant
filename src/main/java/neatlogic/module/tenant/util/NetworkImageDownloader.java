/*
 * Copyright (C) 2026 TechSure Co., Ltd. All Rights Reserved.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 */
package neatlogic.module.tenant.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.exception.file.EmptyFileException;
import neatlogic.framework.exception.file.FileExtNotAllowedException;
import neatlogic.framework.exception.file.FileTooLargeException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.DnsResolver;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

/** 下载公网图片到临时文件，统一处理访问范围、附件限制和资源释放。 */
public class NetworkImageDownloader {
    private static final Logger logger = LoggerFactory.getLogger(NetworkImageDownloader.class);
    private static final int MAX_REDIRECTS = 5;

    /** 下载时统计实际字节数，响应未声明长度时仍可执行大小限制。 */
    public DownloadedImage download(String url, JSONObject config) throws Exception {
        URI current = validateUri(url);
        Path temporaryFile = null;
        long maxSize = config == null ? 0 : config.getLongValue("maxSize");
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
                            throw new ApiRuntimeException("图片下载重定向次数超限或缺少目标地址");
                        }
                        // 每一跳重新校验，禁止重定向绕过公网和协议限制。
                        current = validateUri(current.resolve(location.getValue()).toString());
                        continue;
                    }
                    if (status != 200) {
                        throw new ApiRuntimeException("图片下载失败，HTTP状态码：" + status);
                    }
                    HttpEntity entity = response.getEntity();
                    Header typeHeader = response.getFirstHeader("Content-Type");
                    String contentType = typeHeader == null ? "" : typeHeader.getValue().split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
                    if (!contentType.matches("image/[a-z0-9!#$&^_.+\\-]+")) {
                        throw new ApiRuntimeException("网络地址返回的内容不是图片");
                    }
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
                    return new DownloadedImage(temporaryFile, name, contentType, size);
                }
            }
        } catch (IOException ex) {
            deleteTemporaryFile(temporaryFile);
            throw new ApiRuntimeException("下载网络图片失败：" + ex.getMessage(), ex);
        } catch (Exception | Error ex) {
            deleteTemporaryFile(temporaryFile);
            throw ex;
        }
    }

    /** 使用实际连接的 DNS 解析结果执行校验，并保持默认 TLS 证书校验。 */
    protected CloseableHttpClient createHttpClient() {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(10000)
                .setConnectionRequestTimeout(10000).setSocketTimeout(30000).build();
        return HttpClients.custom().setDnsResolver(validatingDnsResolver(SystemDefaultDnsResolver.INSTANCE))
                .setDefaultRequestConfig(config).disableRedirectHandling().disableAutomaticRetries()
                .disableCookieManagement().disableAuthCaching().disableContentCompression().build();
    }

    /** 使用系统临时目录，避免整张图片占用堆内存。 */
    protected Path createTemporaryFile() throws IOException {
        return Files.createTempFile("neatlogic-network-image-", ".tmp");
    }

    /** 校验协议和地址字面量，并原样保留编码后的查询参数、移除 fragment。 */
    public static URI validateUri(String url) {
        try {
            if (StringUtils.isBlank(url)) {
                throw new ApiRuntimeException("图片地址不能为空");
            }
            URI uri = new URI(url.trim());
            String scheme = uri.getScheme();
            if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    || StringUtils.isBlank(uri.getHost()) || uri.getRawUserInfo() != null
                    || uri.getPort() == 0 || uri.getPort() > 65535) {
                throw new ApiRuntimeException("图片地址必须是不含用户凭据的公网 HTTP/HTTPS 地址");
            }
            String host = uri.getHost();
            if (host.startsWith("[") && host.endsWith("]")) {
                host = host.substring(1, host.length() - 1);
            }
            // HttpClient 对 IP 字面量可能跳过 DNS；数字主机也包含 0.0.0.0、缩写和整数形式。
            if (host.contains(":") || host.matches("[0-9.]+")) {
                if (host.contains("%") || !isPublicAddress(InetAddress.getByName(host))) {
                    throw new ApiRuntimeException("图片地址不能访问非公网地址");
                }
            }
            String raw = uri.toString();
            int fragment = raw.indexOf('#');
            return fragment < 0 ? uri : new URI(raw.substring(0, fragment));
        } catch (URISyntaxException | UnknownHostException | IllegalArgumentException ex) {
            throw new ApiRuntimeException("图片地址格式不正确", ex);
        }
    }

    /** 返回通过安全校验的同一组地址，避免校验和连接分别解析产生竞态。 */
    public static DnsResolver validatingDnsResolver(DnsResolver delegate) {
        return host -> {
            InetAddress[] addresses = delegate.resolve(host);
            if (addresses == null || addresses.length == 0) {
                throw new UnknownHostException("图片域名没有可用地址");
            }
            for (InetAddress address : addresses) {
                if (!isPublicAddress(address)) {
                    throw new UnknownHostException("图片域名不能解析到非公网地址");
                }
            }
            return addresses;
        };
    }

    /** 仅接受公网 IPv4 或全球单播 IPv6，排除保留、文档和隧道地址。 */
    public static boolean isPublicAddress(InetAddress address) {
        if (address == null || address.isAnyLocalAddress() || address.isLoopbackAddress()
                || address.isLinkLocalAddress() || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return false;
        }
        byte[] bytes = address.getAddress();
        if (bytes.length == 4) {
            int a = bytes[0] & 255;
            int b = bytes[1] & 255;
            int c = bytes[2] & 255;
            return !(a == 0 || a == 10 || a == 127 || a >= 224
                    || (a == 100 && b >= 64 && b <= 127)
                    || (a == 169 && b == 254) || (a == 172 && b >= 16 && b <= 31)
                    || (a == 192 && (b == 168 || (b == 0 && (c == 0 || c == 2)) || (b == 88 && c == 99)))
                    || (a == 198 && (b == 18 || b == 19 || (b == 51 && c == 100)))
                    || (a == 203 && b == 0 && c == 113));
        }
        if (bytes.length == 16) {
            // IPv4 映射地址按其内嵌 IPv4 再次校验，不能通过 IPv6 包装绕过限制。
            boolean mapped = true;
            for (int i = 0; i < 10; i++) {
                mapped &= bytes[i] == 0;
            }
            if (mapped && (bytes[10] & 255) == 255 && (bytes[11] & 255) == 255) {
                try {
                    return isPublicAddress(InetAddress.getByAddress(new byte[]{bytes[12], bytes[13], bytes[14], bytes[15]}));
                } catch (UnknownHostException ex) {
                    return false;
                }
            }
            int a = bytes[0] & 255;
            int b = bytes[1] & 255;
            int c = bytes[2] & 255;
            int d = bytes[3] & 255;
            return (a & 0xe0) == 0x20
                    && !(a == 0x20 && b == 0x01 && (c <= 1 || (c == 0x0d && d == 0xb8)))
                    && !(a == 0x20 && b == 0x02)
                    && !(a == 0x3f && b == 0xff && (c & 0xf0) == 0);
        }
        return false;
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
        if (name.lastIndexOf('.') <= 0) {
            String subtype = contentType.substring("image/".length());
            String extension;
            switch (subtype) {
                case "jpeg": case "pjpeg": extension = "jpg"; break;
                case "svg+xml": extension = "svg"; break;
                case "x-icon": case "vnd.microsoft.icon": extension = "ico"; break;
                case "x-png": extension = "png"; break;
                case "x-ms-bmp": extension = "bmp"; break;
                case "tiff": extension = "tif"; break;
                default: extension = subtype.replaceAll("[^a-z0-9]", ""); break;
            }
            name += "." + extension;
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
                logger.error("删除网络图片临时文件失败：{}", file, ex);
            }
        }
    }

    /** 临时下载结果同时持有文件生命周期，调用方使用后自动删除。 */
    public static class DownloadedImage implements AutoCloseable {
        private final Path file;
        private final String name;
        private final String contentType;
        private final long size;

        /** 记录已完整下载并通过附件规则校验的临时图片。 */
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

        /** 返回实际读取的字节数。 */
        public long getSize() { return size; }

        /** 使用完成后删除临时文件，清理异常不改变接口业务结果。 */
        @Override
        public void close() { deleteTemporaryFile(file); }
    }
}
