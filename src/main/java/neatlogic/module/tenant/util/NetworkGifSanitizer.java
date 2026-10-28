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
import neatlogic.framework.util.$;
import neatlogic.module.tenant.exception.file.NetworkImageAnimationLimitException;
import neatlogic.module.tenant.exception.file.NetworkImageEncodingException;
import neatlogic.module.tenant.exception.file.NetworkImageFormatMismatchException;
import neatlogic.module.tenant.exception.file.NetworkImageInvalidContentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/** 对 GIF 先做有界结构清理，再逐帧解码并重建白名单动画数据，保留全部动画帧。 */
public class NetworkGifSanitizer {
    private static final Logger logger = LoggerFactory.getLogger(NetworkGifSanitizer.class);
    private static final String STREAM_FORMAT = "javax_imageio_gif_stream_1.0";
    private static final String IMAGE_FORMAT = "javax_imageio_gif_image_1.0";
    private static final int MAX_FRAMES = 200;
    private static final long MAX_ANIMATION_PIXELS = 200000000L;
    private static final String[] DISPOSALS = {"none", "doNotDispose", "restoreToBackgroundColor", "restoreToPrevious"};

    /** 检查画布尺寸和累计画布像素，边界值允许，运算使用 long 防止整数溢出。 */
    public static void validateAnimationLimits(int width, int height, int frameCount) {
        NetworkImageSanitizer.validateDimensions(width, height);
        if (frameCount < 1 || frameCount > MAX_FRAMES || (long) width * height * frameCount > MAX_ANIMATION_PIXELS) {
            throw new NetworkImageAnimationLimitException(width, height, frameCount);
        }
    }

    /** 内部结构临时文件自行管理，原始和最终文件的生命周期由外部下载器管理。 */
    public void sanitize(Path source, Path target) {
        Path cleanedSource = null;
        try {
            GifStructure structure;
            try {
                cleanedSource = createTemporaryFile();
                structure = cleanStructure(source, cleanedSource);
            } catch (ApiRuntimeException ex) {
                throw ex;
            } catch (IOException | RuntimeException ex) {
                throw new NetworkImageInvalidContentException(ex);
            }
            try {
                writeSequence(cleanedSource, target, structure);
                if (structure.globalPalette == null) {
                    // JDK sequence writer 会自动提升首帧调色板，写后恢复源文件无全局表的背景语义。
                    restoreLocalPalettes(target, cleanedSource, structure);
                }
                if (Files.size(target) == 0) {
                    throw new IOException("GIF encoder produced an empty file");
                }
            } catch (ApiRuntimeException ex) {
                throw ex;
            } catch (IOException | RuntimeException ex) {
                throw new NetworkImageEncodingException(ex);
            }
        } finally {
            if (cleanedSource != null) {
                try {
                    Files.deleteIfExists(cleanedSource);
                } catch (IOException ex) {
                    logger.error($.t("nmtu.networkimagedownloader.deletetemporaryfile"), cleanedSource, ex);
                }
            }
        }
    }

    /** 使用单独结构临时文件，避免 GIF reader 将任意长度扩展数据拼接到内存。 */
    protected Path createTemporaryFile() throws IOException {
        return Files.createTempFile("neatlogic-network-gif-structure-", ".gif");
    }

    /** 逐帧完整读取并重建索引色像素，不在内存中同时保存整段动画。 */
    protected void writeSequence(Path cleanedSource, Path target, GifStructure structure) throws IOException {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("gif");
        if (!readers.hasNext() || !writers.hasNext()) {
            throw new IOException("GIF reader or writer unavailable");
        }
        try (SeekableByteChannel channel = Files.newByteChannel(target, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            channel.truncate(0);
        }
        ImageReader reader = readers.next();
        ImageWriter writer = writers.next();
        try (ImageInputStream input = new FileImageInputStream(cleanedSource.toFile());
             ImageOutputStream output = new FileImageOutputStream(target.toFile())) {
            boolean[] readWarning = {false};
            boolean[] writeWarning = {false};
            reader.addIIOReadWarningListener((ignored, warning) -> readWarning[0] = true);
            writer.addIIOWriteWarningListener((ignored, index, warning) -> writeWarning[0] = true);
            reader.setInput(input, false, true);
            if (!"gif".equalsIgnoreCase(reader.getFormatName())) {
                throw new NetworkImageFormatMismatchException();
            }
            writer.setOutput(output);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteProgressive()) {
                param.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
            }
            writer.prepareWriteSequence(streamMetadata(writer, param, structure));
            for (int i = 0; i < structure.frames.size(); i++) {
                Frame frame = structure.frames.get(i);
                BufferedImage decoded = null;
                BufferedImage sanitized = null;
                try {
                    try {
                        if (reader.getWidth(i) != frame.width || reader.getHeight(i) != frame.height) {
                            throw new NetworkImageInvalidContentException();
                        }
                        decoded = reader.read(i);
                        if (readWarning[0] || decoded == null || decoded.getWidth() != frame.width || decoded.getHeight() != frame.height) {
                            throw new NetworkImageInvalidContentException();
                        }
                        sanitized = rebuildPixels(decoded, frame, structure);
                    } catch (ApiRuntimeException ex) {
                        throw ex;
                    } catch (IOException | RuntimeException ex) {
                        throw new NetworkImageInvalidContentException(ex);
                    }
                    // 只重建帧描述、调色板、GCE 和第一帧的循环扩展，原 metadata 不传给 writer。
                    IIOMetadata metadata = frameMetadata(writer, param, sanitized, frame, structure, i == 0);
                    writer.writeToSequence(new IIOImage(sanitized, null, metadata), param);
                    if (writeWarning[0]) {
                        throw new IOException("GIF encoder reported a warning");
                    }
                } finally {
                    if (decoded != null) {
                        decoded.flush();
                    }
                    if (sanitized != null) {
                        sanitized.flush();
                    }
                }
            }
            writer.endWriteSequence();
            output.flush();
        } finally {
            try {
                reader.dispose();
            } finally {
                writer.dispose();
            }
        }
    }

    /** 固定缓冲区扫描完整 GIF 结构，未知扩展和尾部仅丢弃，不交给 ImageIO。 */
    private GifStructure cleanStructure(Path source, Path cleanedSource) throws IOException {
        GifStructure structure = new GifStructure();
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(Files.newInputStream(source)));
             DataOutputStream output = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(cleanedSource)))) {
            byte[] signature = input.readNBytes(6);
            if (!Arrays.equals(signature, "GIF87a".getBytes(StandardCharsets.US_ASCII))
                    && !Arrays.equals(signature, "GIF89a".getBytes(StandardCharsets.US_ASCII))) {
                // 可识别的其他图片格式属于声明不一致，未知或截断签名属于内容损坏。
                if ((signature.length >= 3 && (signature[0] & 255) == 255
                        && (signature[1] & 255) == 216 && (signature[2] & 255) == 255)
                        || Arrays.equals(signature, new byte[]{(byte) 137, 80, 78, 71, 13, 10})) {
                    throw new NetworkImageFormatMismatchException();
                }
                throw new NetworkImageInvalidContentException();
            }
            output.write("GIF89a".getBytes(StandardCharsets.US_ASCII));
            structure.width = readWord(input);
            structure.height = readWord(input);
            validateAnimationLimits(structure.width, structure.height, 1);
            int packed = input.readUnsignedByte();
            structure.colorResolution = ((packed >> 4) & 7) + 1;
            structure.backgroundIndex = input.readUnsignedByte();
            structure.pixelAspectRatio = input.readUnsignedByte();
            writeWord(output, structure.width);
            writeWord(output, structure.height);
            output.writeByte(packed);
            output.writeByte(structure.backgroundIndex);
            output.writeByte(structure.pixelAspectRatio);
            if ((packed & 128) != 0) {
                structure.globalPalette = readPalette(input, packed);
                if (structure.backgroundIndex >= structure.globalPalette.length / 3) {
                    throw new NetworkImageInvalidContentException();
                }
                output.write(structure.globalPalette);
            }
            Control pendingControl = null;
            while (true) {
                int marker = input.readUnsignedByte();
                if (marker == 0x3b) {
                    if (structure.frames.isEmpty() || pendingControl != null) {
                        throw new NetworkImageInvalidContentException();
                    }
                    output.writeByte(marker);
                    return structure;
                }
                if (marker == 0x21) {
                    int label = input.readUnsignedByte();
                    if (label == 0xf9) {
                        if (pendingControl != null) {
                            throw new NetworkImageInvalidContentException();
                        }
                        pendingControl = readControl(input);
                    } else if (label == 0xff) {
                        readApplication(input, output, structure);
                    } else if (label == 0x01) {
                        // 文本绘制无法通过图片像素完整保留，拒绝而非静默丢弃显示内容。
                        throw new NetworkImageInvalidContentException();
                    } else {
                        copySubBlocks(input, null, null);
                    }
                    continue;
                }
                if (marker != 0x2c) {
                    throw new NetworkImageInvalidContentException();
                }
                Frame frame = new Frame();
                frame.left = readWord(input);
                frame.top = readWord(input);
                frame.width = readWord(input);
                frame.height = readWord(input);
                NetworkImageSanitizer.validateDimensions(frame.width, frame.height);
                if ((long) frame.left + frame.width > structure.width || (long) frame.top + frame.height > structure.height) {
                    throw new NetworkImageInvalidContentException();
                }
                validateAnimationLimits(structure.width, structure.height, structure.frames.size() + 1);
                int imagePacked = input.readUnsignedByte();
                if ((imagePacked & 0x18) != 0) {
                    throw new NetworkImageInvalidContentException();
                }
                frame.control = pendingControl == null ? new Control() : pendingControl;
                pendingControl = null;
                if ((imagePacked & 128) != 0) {
                    frame.localPalette = readPalette(input, imagePacked);
                }
                byte[] palette = frame.localPalette == null ? structure.globalPalette : frame.localPalette;
                if (palette == null || (frame.control.transparent && frame.control.transparentIndex >= palette.length / 3)) {
                    throw new NetworkImageInvalidContentException();
                }
                writeControl(output, frame.control);
                output.writeByte(marker);
                writeWord(output, frame.left);
                writeWord(output, frame.top);
                writeWord(output, frame.width);
                writeWord(output, frame.height);
                output.writeByte(imagePacked);
                if (frame.localPalette != null) {
                    output.write(frame.localPalette);
                }
                int minimumCodeSize = input.readUnsignedByte();
                if (minimumCodeSize < 2 || minimumCodeSize > 8) {
                    throw new NetworkImageInvalidContentException();
                }
                output.writeByte(minimumCodeSize);
                LzwValidator lzw = new LzwValidator(minimumCodeSize, palette.length / 3, (long) frame.width * frame.height);
                copySubBlocks(input, output, lzw);
                lzw.finish();
                structure.frames.add(frame);
            }
        }
    }

    /** GCE 固定四字节，延时原样保留；保留位和未定义处置方式不能进入标准输出。 */
    private Control readControl(DataInputStream input) throws IOException {
        if (input.readUnsignedByte() != 4) {
            throw new NetworkImageInvalidContentException();
        }
        int packed = input.readUnsignedByte();
        Control control = new Control();
        control.disposal = (packed >> 2) & 7;
        control.userInput = (packed & 2) != 0;
        control.transparent = (packed & 1) != 0;
        control.delay = readWord(input);
        control.transparentIndex = input.readUnsignedByte();
        if ((packed & 0xe0) != 0 || control.disposal > 3 || input.readUnsignedByte() != 0) {
            throw new NetworkImageInvalidContentException();
        }
        return control;
    }

    /** 重建图形控制扩展，仅保留处置、延时、透明索引和已有用户输入标志。 */
    private void writeControl(DataOutputStream output, Control control) throws IOException {
        output.writeByte(0x21);
        output.writeByte(0xf9);
        output.writeByte(4);
        output.writeByte((control.disposal << 2) | (control.userInput ? 2 : 0) | (control.transparent ? 1 : 0));
        writeWord(output, control.delay);
        output.writeByte(control.transparent ? control.transparentIndex : 0);
        output.writeByte(0);
    }

    /** 只有规范循环扩展保留，未知 application 以有界子块跳过。 */
    private void readApplication(DataInputStream input, DataOutputStream output, GifStructure structure) throws IOException {
        int headerLength = input.readUnsignedByte();
        byte[] identifier = input.readNBytes(headerLength);
        if (identifier.length != headerLength || headerLength != 11) {
            throw new NetworkImageInvalidContentException();
        }
        String application = new String(identifier, StandardCharsets.US_ASCII);
        if (!"NETSCAPE2.0".equals(application) && !"ANIMEXTS1.0".equals(application)) {
            copySubBlocks(input, null, null);
            return;
        }
        if (input.readUnsignedByte() != 3 || input.readUnsignedByte() != 1) {
            throw new NetworkImageInvalidContentException();
        }
        int loopCount = readWord(input);
        if (input.readUnsignedByte() != 0) {
            throw new NetworkImageInvalidContentException();
        }
        // 部分编码器逐帧重复同一循环设置，只保留第一次；冲突设置不能确定播放语义。
        if (structure.loopCount != null) {
            if (structure.loopCount != loopCount) {
                throw new NetworkImageInvalidContentException();
            }
            return;
        }
        structure.loopCount = loopCount;
        // 结构临时文件也只使用规范 NETSCAPE 循环块，避免 reader 拼接任意扩展。
        output.writeByte(0x21);
        output.writeByte(0xff);
        output.writeByte(11);
        output.write("NETSCAPE2.0".getBytes(StandardCharsets.US_ASCII));
        output.writeByte(3);
        output.writeByte(1);
        writeWord(output, structure.loopCount);
        output.writeByte(0);
    }

    /** 固定缓冲区读取子块；未知扩展丢弃，图像数据额外执行完整 LZW 校验。 */
    private void copySubBlocks(DataInputStream input, DataOutputStream output, LzwValidator lzw) throws IOException {
        byte[] buffer = new byte[255];
        while (true) {
            int length = input.readUnsignedByte();
            if (output != null) {
                output.writeByte(length);
            }
            if (length == 0) {
                return;
            }
            input.readFully(buffer, 0, length);
            if (lzw != null) {
                for (int i = 0; i < length; i++) {
                    lzw.accept(buffer[i] & 255);
                }
            }
            if (output != null) {
                output.write(buffer, 0, length);
            }
        }
    }

    /** GIF 调色板最多 256 色，读取长度固定有界。 */
    private byte[] readPalette(DataInputStream input, int packed) throws IOException {
        byte[] palette = new byte[3 * (1 << ((packed & 7) + 1))];
        input.readFully(palette);
        return palette;
    }

    /** GIF 多字节数值为无符号小端，使用固定两字节读取。 */
    private int readWord(DataInputStream input) throws IOException {
        return input.readUnsignedByte() | (input.readUnsignedByte() << 8);
    }

    /** 重新写出无符号小端数值，避免依赖平台字节序。 */
    private void writeWord(DataOutputStream output, int value) throws IOException {
        output.writeByte(value & 255);
        output.writeByte((value >> 8) & 255);
    }

    /** 按原始调色板构造全新索引色图像，复制索引而非量化 RGB，保留透明和背景语义。 */
    private BufferedImage rebuildPixels(BufferedImage decoded, Frame frame, GifStructure structure) {
        byte[] palette = frame.localPalette == null ? structure.globalPalette : frame.localPalette;
        int count = palette.length / 3;
        byte[] red = new byte[count];
        byte[] green = new byte[count];
        byte[] blue = new byte[count];
        for (int i = 0; i < count; i++) {
            red[i] = palette[3 * i];
            green[i] = palette[3 * i + 1];
            blue[i] = palette[3 * i + 2];
        }
        int bits = Integer.numberOfTrailingZeros(Integer.highestOneBit(count));
        IndexColorModel model = frame.control.transparent
                ? new IndexColorModel(bits, count, red, green, blue, frame.control.transparentIndex)
                : new IndexColorModel(bits, count, red, green, blue);
        WritableRaster raster = model.createCompatibleWritableRaster(frame.width, frame.height);
        BufferedImage result = new BufferedImage(model, raster, false, null);
        int[] row = new int[frame.width];
        for (int y = 0; y < frame.height; y++) {
            decoded.getRaster().getSamples(0, y, frame.width, 1, 0, row);
            for (int index : row) {
                if (index < 0 || index >= count) {
                    result.flush();
                    throw new NetworkImageInvalidContentException();
                }
            }
            raster.setSamples(0, y, frame.width, 1, 0, row);
        }
        return result;
    }

    /** 重建 GIF89a 画布及全局调色板，保留 disposal=2 使用的背景索引。 */
    private IIOMetadata streamMetadata(ImageWriter writer, ImageWriteParam param, GifStructure structure) throws IOException {
        IIOMetadata metadata = writer.getDefaultStreamMetadata(param);
        IIOMetadataNode root = new IIOMetadataNode(STREAM_FORMAT);
        node(root, "Version", "value", "89a");
        IIOMetadataNode screen = node(root, "LogicalScreenDescriptor", "logicalScreenWidth", structure.width);
        screen.setAttribute("logicalScreenHeight", Integer.toString(structure.height));
        screen.setAttribute("colorResolution", Integer.toString(structure.colorResolution));
        screen.setAttribute("pixelAspectRatio", Integer.toString(structure.pixelAspectRatio));
        if (structure.globalPalette != null) {
            IIOMetadataNode table = paletteNode(root, "GlobalColorTable", structure.globalPalette);
            table.setAttribute("backgroundColorIndex", Integer.toString(structure.backgroundIndex));
        }
        metadata.setFromTree(STREAM_FORMAT, root);
        return metadata;
    }

    /** 帧 metadata 只含描述、局部调色板、GCE 及经校验的循环次数，不复制源树。 */
    private IIOMetadata frameMetadata(ImageWriter writer, ImageWriteParam param, BufferedImage image,
                                      Frame frame, GifStructure structure, boolean first) throws IOException {
        IIOMetadata metadata = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(image), param);
        IIOMetadataNode root = new IIOMetadataNode(IMAGE_FORMAT);
        IIOMetadataNode descriptor = node(root, "ImageDescriptor", "imageLeftPosition", frame.left);
        descriptor.setAttribute("imageTopPosition", Integer.toString(frame.top));
        descriptor.setAttribute("imageWidth", Integer.toString(frame.width));
        descriptor.setAttribute("imageHeight", Integer.toString(frame.height));
        descriptor.setAttribute("interlaceFlag", "FALSE");
        if (frame.localPalette != null) {
            paletteNode(root, "LocalColorTable", frame.localPalette);
        }
        IIOMetadataNode control = node(root, "GraphicControlExtension", "disposalMethod", DISPOSALS[frame.control.disposal]);
        control.setAttribute("userInputFlag", frame.control.userInput ? "TRUE" : "FALSE");
        control.setAttribute("transparentColorFlag", frame.control.transparent ? "TRUE" : "FALSE");
        control.setAttribute("delayTime", Integer.toString(frame.control.delay));
        control.setAttribute("transparentColorIndex", Integer.toString(frame.control.transparent ? frame.control.transparentIndex : 0));
        if (first && structure.loopCount != null) {
            IIOMetadataNode applications = new IIOMetadataNode("ApplicationExtensions");
            IIOMetadataNode application = node(applications, "ApplicationExtension", "applicationID", "NETSCAPE");
            application.setAttribute("authenticationCode", "2.0");
            application.setUserObject(new byte[]{1, (byte) (structure.loopCount & 255), (byte) ((structure.loopCount >> 8) & 255)});
            root.appendChild(applications);
        }
        metadata.setFromTree(IMAGE_FORMAT, root);
        return metadata;
    }

    /** 以白名单索引和 RGB 数值重建调色板节点。 */
    private IIOMetadataNode paletteNode(IIOMetadataNode parent, String name, byte[] palette) {
        IIOMetadataNode table = node(parent, name, "sizeOf" + name, palette.length / 3);
        table.setAttribute("sortFlag", "FALSE");
        for (int i = 0; i < palette.length / 3; i++) {
            IIOMetadataNode color = node(table, "ColorTableEntry", "index", i);
            color.setAttribute("red", Integer.toString(palette[3 * i] & 255));
            color.setAttribute("green", Integer.toString(palette[3 * i + 1] & 255));
            color.setAttribute("blue", Integer.toString(palette[3 * i + 2] & 255));
        }
        return table;
    }

    /** 创建只包含指定属性的 metadata 子节点。 */
    private IIOMetadataNode node(IIOMetadataNode parent, String name, String attribute, Object value) {
        IIOMetadataNode child = new IIOMetadataNode(name);
        child.setAttribute(attribute, String.valueOf(value));
        parent.appendChild(child);
        return child;
    }

    /** 复用已不再读取的结构临时文件，将 writer 提升的全局表移回局部表，不增加临时文件数量。 */
    private void restoreLocalPalettes(Path encoded, Path scratch, GifStructure structure) throws IOException {
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(Files.newInputStream(encoded)))) {
            byte[] header = new byte[13];
            input.readFully(header);
            int packed = header[10] & 255;
            if ((packed & 128) == 0) {
                return;
            }
            byte[] generatedPalette = readPalette(input, packed);
            header[10] = (byte) ((structure.colorResolution - 1) << 4);
            header[11] = (byte) structure.backgroundIndex;
            header[12] = (byte) structure.pixelAspectRatio;
            try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(scratch)))) {
                output.write(header);
                while (true) {
                    int marker = input.readUnsignedByte();
                    output.writeByte(marker);
                    if (marker == 0x3b) {
                        break;
                    }
                    if (marker == 0x21) {
                        // 此处输入仅来自受控 writer，扩展数据已由白名单 metadata 生成。
                        output.writeByte(input.readUnsignedByte());
                        copySubBlocks(input, output, null);
                    } else if (marker == 0x2c) {
                        byte[] descriptor = new byte[9];
                        input.readFully(descriptor);
                        int imagePacked = descriptor[8] & 255;
                        byte[] palette;
                        if ((imagePacked & 128) != 0) {
                            palette = readPalette(input, imagePacked);
                        } else {
                            palette = generatedPalette;
                            int sizeBits = Integer.numberOfTrailingZeros(palette.length / 3) - 1;
                            descriptor[8] = (byte) (imagePacked | 128 | sizeBits);
                        }
                        output.write(descriptor);
                        output.write(palette);
                        output.writeByte(input.readUnsignedByte());
                        copySubBlocks(input, output, null);
                    } else {
                        throw new IOException("Unexpected block in re-encoded GIF");
                    }
                }
            }
        }
        Files.copy(scratch, encoded, StandardCopyOption.REPLACE_EXISTING);
    }

    /** 结构清理只保存有界的画布、调色板、循环和帧描述，不缓存所有帧像素。 */
    protected static class GifStructure {
        private int width;
        private int height;
        private int backgroundIndex;
        private int colorResolution;
        private int pixelAspectRatio;
        private byte[] globalPalette;
        private Integer loopCount;
        private final List<Frame> frames = new ArrayList<>();
    }

    /** 一帧的白名单几何和索引色信息，最多保存 200 份。 */
    private static class Frame {
        private int left;
        private int top;
        private int width;
        private int height;
        private byte[] localPalette;
        private Control control;
    }

    /** 缺失 GCE 时采用规范默认值，已有的用户输入标志作为有限标量保留。 */
    private static class Control {
        private int disposal;
        private int delay;
        private boolean transparent;
        private boolean userInput;
        private int transparentIndex;
    }

    /** 用固定大小字典验证 LZW 的码流终止和精确像素数，不展开图像字符串。 */
    private static class LzwValidator {
        private final int minimumCodeSize;
        private final int paletteSize;
        private final long expectedPixels;
        private final int clearCode;
        private final int endCode;
        private final int[] lengths = new int[4096];
        private int codeSize;
        private int nextCode;
        private int previousLength;
        private int bitBuffer;
        private int bitCount;
        private long pixels;
        private boolean started;
        private boolean ended;

        /** 创建至多 4096 项的字典长度检查器。 */
        private LzwValidator(int minimumCodeSize, int paletteSize, long expectedPixels) {
            this.minimumCodeSize = minimumCodeSize;
            this.paletteSize = paletteSize;
            this.expectedPixels = expectedPixels;
            this.clearCode = 1 << minimumCodeSize;
            this.endCode = clearCode + 1;
            reset();
        }

        /** 按 GIF 低位在前的位序消费字节，结束标记之后不解析附加压缩内容。 */
        private void accept(int value) {
            if (ended) {
                return;
            }
            bitBuffer |= value << bitCount;
            bitCount += 8;
            while (bitCount >= codeSize && !ended) {
                int code = bitBuffer & ((1 << codeSize) - 1);
                bitBuffer >>>= codeSize;
                bitCount -= codeSize;
                // 初始码表已经就绪，首码可为有效字面量，不强制要求编码器先发清码。
                started = true;
                if (code == clearCode) {
                    reset();
                    continue;
                }
                if (code == endCode) {
                    ended = true;
                    continue;
                }
                int length;
                if (code < clearCode && code < paletteSize) {
                    length = 1;
                } else if (code > endCode && code < nextCode && previousLength > 0) {
                    length = lengths[code];
                } else if (code == nextCode && previousLength > 0 && nextCode < 4096) {
                    length = previousLength + 1;
                } else {
                    throw new NetworkImageInvalidContentException();
                }
                pixels += length;
                if (pixels > expectedPixels) {
                    throw new NetworkImageInvalidContentException();
                }
                if (previousLength > 0 && nextCode < 4096) {
                    lengths[nextCode++] = previousLength + 1;
                    if (nextCode == (1 << codeSize) && codeSize < 12) {
                        codeSize++;
                    }
                }
                previousLength = length;
            }
        }

        /** 清码不清空位缓冲区和像素累计，只重置当前码表。 */
        private void reset() {
            codeSize = minimumCodeSize + 1;
            nextCode = endCode + 1;
            previousLength = 0;
        }

        /** 完整帧必须准确产生描述尺寸的像素并具有 LZW 结束码。 */
        private void finish() {
            if (!started || !ended || pixels != expectedPixels) {
                throw new NetworkImageInvalidContentException();
            }
        }
    }
}
