/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.util;

import neatlogic.framework.util.ImageSafeUtil;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 将图片流保存为请求内的临时快照，确保校验与输出使用同一份内容。
 */
public final class ImageFileSnapshot implements AutoCloseable {
    private final Path path;
    private final String contentType;

    /** 保存已验证的快照及服务端识别的媒体类型。 */
    private ImageFileSnapshot(Path path, String contentType) {
        this.path = path;
        this.contentType = contentType;
    }

    /** 复制并校验输入流；输入流由调用者关闭，失败时立即清理临时文件。 */
    public static ImageFileSnapshot create(InputStream input, String fileName) throws IOException {
        return create(input, fileName, true);
    }

    /** 下载历史图片按实际内容识别，兼容已有文件名与真实格式不一致的记录。 */
    public static ImageFileSnapshot createForDownload(InputStream input) throws IOException {
        return create(input, null, false);
    }

    /** 共用快照及清理流程，上传额外要求扩展名与真实格式一致。 */
    private static ImageFileSnapshot create(InputStream input, String fileName, boolean validateFileName) throws IOException {
        Path path = Files.createTempFile("neatlogic-image-", ".tmp");
        try {
            Files.copy(input, path, StandardCopyOption.REPLACE_EXISTING);
            try (InputStream snapshot = Files.newInputStream(path)) {
                String contentType;
                if (validateFileName) {
                    contentType = ImageSafeUtil.validateImage(snapshot, fileName);
                } else {
                    contentType = ImageSafeUtil.validateImageContent(snapshot);
                }
                return new ImageFileSnapshot(path, contentType);
            }
        } catch (IOException | RuntimeException | Error ex) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException cleanupException) {
                ex.addSuppressed(cleanupException);
            }
            throw ex;
        }
    }

    /** 获取由文件内容确认的媒体类型。 */
    public String getContentType() {
        return contentType;
    }

    /** 从已验证快照读取原始图片，保留动画帧及原始编码。 */
    public InputStream openStream() throws IOException {
        return Files.newInputStream(path);
    }

    /** 完成验证后才设置响应头并输出，避免历史污染附件作为图片返回。 */
    public void writeTo(HttpServletResponse response) throws IOException {
        response.setContentType(contentType);
        response.setHeader("X-Content-Type-Options", "nosniff");
        try (InputStream input = openStream(); ServletOutputStream output = response.getOutputStream()) {
            IOUtils.copyLarge(input, output);
            output.flush();
        }
    }

    /** 请求结束后删除临时快照。 */
    @Override
    public void close() throws IOException {
        Files.deleteIfExists(path);
    }
}
