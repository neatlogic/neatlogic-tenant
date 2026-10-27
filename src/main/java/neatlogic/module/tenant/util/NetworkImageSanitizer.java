/*
 * Copyright (C) 2025  TechSure Co., Ltd. All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package neatlogic.module.tenant.util;

import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.module.tenant.exception.file.NetworkImageContentTypeException;
import neatlogic.module.tenant.exception.file.NetworkImageDimensionsException;
import neatlogic.module.tenant.exception.file.NetworkImageEncodingException;
import neatlogic.module.tenant.exception.file.NetworkImageFormatMismatchException;
import neatlogic.module.tenant.exception.file.NetworkImageInvalidContentException;
import neatlogic.module.tenant.exception.file.NetworkImageUnsupportedFormatException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.zip.CRC32;

/** 完整解码并重新编码 JPEG/PNG/GIF，重建安全像素和必要动画信息，移除附加内容。 */
public class NetworkImageSanitizer {
    private static final byte[] PNG_SIGNATURE = {(byte) 137, 80, 78, 71, 13, 10, 26, 10};
    private static final int MAX_DIMENSION = 10000;
    private static final long MAX_PIXELS = 20000000L;

    /** 规范化响应类型；非图片沿用原异常，其余不支持的图片类型单独报告。 */
    public static String normalizeContentType(String raw) {
        String type = raw == null ? "" : raw.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        if (!type.matches("image/[a-z0-9!#$&^_.+\\-]+")) {
            throw new NetworkImageContentTypeException();
        }
        if ("image/jpeg".equals(type) || "image/pjpeg".equals(type)) {
            return "image/jpeg";
        }
        if ("image/png".equals(type) || "image/x-png".equals(type)) {
            return "image/png";
        }
        if ("image/gif".equals(type)) {
            return "image/gif";
        }
        throw new NetworkImageUnsupportedFormatException(type);
    }

    /** 在分配像素内存前检查单边尺寸和总像素，使用 long 避免乘法溢出。 */
    public static void validateDimensions(int width, int height) {
        if (width <= 0 || height <= 0 || width > MAX_DIMENSION || height > MAX_DIMENSION
                || (long) width * height > MAX_PIXELS) {
            throw new NetworkImageDimensionsException(width, height);
        }
    }

    /** 校验结构、实际解码格式和全部解码告警，再写出无原始元数据的新图片。 */
    public void sanitize(Path source, Path target, String canonicalContentType) {
        String type = normalizeContentType(canonicalContentType);
        if ("image/gif".equals(type)) {
            // 动画采用独立的逐帧处理链路，不能通过通用单帧读取而丢失其余画面。
            createGifSanitizer().sanitize(source, target);
            return;
        }
        BufferedImage decoded = null;
        BufferedImage sanitized = null;
        try {
            validateStructure(source, type);
            try (ImageInputStream input = new FileImageInputStream(source.toFile())) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext()) {
                    throw new NetworkImageInvalidContentException();
                }
                ImageReader reader = readers.next();
                boolean[] warning = {false};
                reader.addIIOReadWarningListener((ignored, message) -> warning[0] = true);
                try {
                    String actual = reader.getFormatName().toLowerCase(Locale.ROOT);
                    if (!("image/jpeg".equals(type) && ("jpeg".equals(actual) || "jpg".equals(actual)))
                            && !("image/png".equals(type) && "png".equals(actual))) {
                        throw new NetworkImageFormatMismatchException();
                    }
                    reader.setInput(input, true, true);
                    int width = reader.getWidth(0);
                    int height = reader.getHeight(0);
                    validateDimensions(width, height);
                    decoded = reader.read(0);
                    if (warning[0] || decoded == null || decoded.getWidth() != width || decoded.getHeight() != height) {
                        throw new NetworkImageInvalidContentException();
                    }
                    // 新建像素图，不传递源图片的 EXIF、ICC、注释或其他 metadata。
                    sanitized = new BufferedImage(width, height,
                            "image/png".equals(type) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics = sanitized.createGraphics();
                    try {
                        graphics.setComposite(AlphaComposite.Src);
                        graphics.drawImage(decoded, 0, 0, null);
                    } finally {
                        graphics.dispose();
                    }
                } finally {
                    reader.dispose();
                }
            } catch (ApiRuntimeException ex) {
                throw ex;
            } catch (IOException | RuntimeException ex) {
                throw new NetworkImageInvalidContentException(ex);
            }
            try {
                writeImage(sanitized, target, "image/jpeg".equals(type) ? "jpeg" : "png");
                if (Files.size(target) == 0) {
                    throw new IOException("Image writer produced an empty file");
                }
            } catch (IOException | RuntimeException ex) {
                throw new NetworkImageEncodingException(ex);
            }
        } catch (IOException ex) {
            throw new NetworkImageInvalidContentException(ex);
        } finally {
            if (decoded != null) {
                decoded.flush();
            }
            if (sanitized != null) {
                sanitized.flush();
            }
        }
    }

    /** 创建独立 GIF 动画校验器，允许验证内部结构文件及编码失败清理路径。 */
    protected NetworkGifSanitizer createGifSanitizer() {
        return new NetworkGifSanitizer();
    }

    /** 以全新 metadata 写出，JPEG 使用 RGB 和显式 0.95 质量，PNG 保留 alpha。 */
    protected void writeImage(BufferedImage image, Path target, String formatName) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);
        if (!writers.hasNext()) {
            throw new IOException("Image writer unavailable: " + formatName);
        }
        // 直接使用目标临时文件作为编码器随机访问输出，避免缓存完整压缩数据到堆内存。
        try (SeekableByteChannel channel = Files.newByteChannel(target, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            channel.truncate(0);
        }
        ImageWriter writer = writers.next();
        try (ImageOutputStream output = new FileImageOutputStream(target.toFile())) {
            writer.setOutput(output);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if ("jpeg".equals(formatName)) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.95f);
            }
            writer.write(null, new IIOImage(image, null, null), param);
            output.flush();
        } finally {
            writer.dispose();
        }
    }

    /** 完整检查 PNG 八字节签名或 JPEG 起始标记，拒绝声明与实际签名不一致。 */
    private void validateStructure(Path source, String type) throws IOException {
        byte[] prefix;
        try (InputStream input = Files.newInputStream(source)) {
            prefix = input.readNBytes(8);
        }
        boolean png = Arrays.equals(PNG_SIGNATURE, prefix);
        boolean jpeg = prefix.length >= 3 && (prefix[0] & 255) == 255
                && (prefix[1] & 255) == 216 && (prefix[2] & 255) == 255;
        if (prefix.length >= 6) {
            String signature = new String(prefix, 0, 6, StandardCharsets.US_ASCII);
            if ("GIF87a".equals(signature) || "GIF89a".equals(signature)) {
                throw new NetworkImageFormatMismatchException();
            }
        }
        if (!png && !jpeg) {
            throw new NetworkImageInvalidContentException();
        }
        if (png != "image/png".equals(type)) {
            throw new NetworkImageFormatMismatchException();
        }
        if (png) {
            validatePngChunks(source);
        } else {
            validateJpegEnd(source);
        }
    }

    /** 顺序检查 PNG 块长度、CRC 和 IEND，拒绝解码器可能容忍的截断或损坏数据。 */
    private void validatePngChunks(Path source) throws IOException {
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(Files.newInputStream(source)))) {
            input.skipNBytes(PNG_SIGNATURE.length);
            boolean first = true;
            boolean seenData = false;
            boolean dataEnded = false;
            byte[] buffer = new byte[8192];
            while (true) {
                long length = Integer.toUnsignedLong(input.readInt());
                byte[] chunkType = input.readNBytes(4);
                if (chunkType.length != 4 || length > Integer.MAX_VALUE) {
                    throw new NetworkImageInvalidContentException();
                }
                String chunk = new String(chunkType, StandardCharsets.US_ASCII);
                if (first && !("IHDR".equals(chunk) && length == 13)) {
                    throw new NetworkImageInvalidContentException();
                }
                if (!first && "IHDR".equals(chunk)) {
                    throw new NetworkImageInvalidContentException();
                }
                first = false;
                if ("IDAT".equals(chunk)) {
                    if (dataEnded) {
                        throw new NetworkImageInvalidContentException();
                    }
                    seenData = true;
                } else if (seenData) {
                    dataEnded = true;
                }
                CRC32 crc = new CRC32();
                crc.update(chunkType);
                long remaining = length;
                while (remaining > 0) {
                    int count = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                    if (count < 0) {
                        throw new NetworkImageInvalidContentException();
                    }
                    crc.update(buffer, 0, count);
                    remaining -= count;
                }
                if (Integer.toUnsignedLong(input.readInt()) != crc.getValue()) {
                    throw new NetworkImageInvalidContentException();
                }
                if ("IEND".equals(chunk)) {
                    if (length != 0 || !seenData) {
                        throw new NetworkImageInvalidContentException();
                    }
                    return;
                }
            }
        }
    }

    /** JPEG 必须包含结束标记；完整像素解码仍负责检查扫描数据与解码告警。 */
    private void validateJpegEnd(Path source) throws IOException {
        try (InputStream input = new BufferedInputStream(Files.newInputStream(source))) {
            int previous = -1;
            int value;
            while ((value = input.read()) != -1) {
                if (previous == 255 && value == 217) {
                    return;
                }
                previous = value;
            }
        }
        throw new NetworkImageInvalidContentException();
    }
}
