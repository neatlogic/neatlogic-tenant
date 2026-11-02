/*
 * Copyright (C) 2026  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.tenant.api.file;

import com.alibaba.fastjson.JSONObject;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.file.core.FileStorageMediumFactory;
import neatlogic.framework.file.core.FileTypeHandlerFactory;
import neatlogic.framework.file.core.IFileStorageHandler;
import neatlogic.framework.file.core.IFileTypeHandler;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/** 使用内存存储和请求替身验证图片入口，避免连接数据库或修改真实附件。 */
public class ImageApiSecurityFixture {
    protected Map<String, IFileTypeHandler> typeRegistry;
    protected Map<String, IFileTypeHandler> originalTypes;
    protected Map<String, IFileStorageHandler> storageRegistry;
    protected Map<String, IFileStorageHandler> originalStorage;
    protected FileMapper fileMapper;
    protected FileVo savedFile;
    protected byte[] storedData;
    protected int avatarUpdates;
    protected Runnable beforeSave = () -> { };
    protected boolean failStorage;
    protected int saveAttempts;

    /** 安装测试存储和 Mapper，并备份所有被替换的全局对象。 */
    @Before
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        typeRegistry = (Map<String, IFileTypeHandler>) field(FileTypeHandlerFactory.class, "componentMap").get(null);
        originalTypes = new HashMap<>(typeRegistry);
        storageRegistry = (Map<String, IFileStorageHandler>) field(FileStorageMediumFactory.class, "componentMap").get(null);
        originalStorage = new HashMap<>(storageRegistry);
        IFileTypeHandler type = (IFileTypeHandler) Proxy.newProxyInstance(IFileTypeHandler.class.getClassLoader(),
                new Class[]{IFileTypeHandler.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getFileStorageHandlerName": return "TEST";
                        case "valid": case "beforeUpload": case "needSave": return true;
                        case "isUnique": return false;
                        default: return null;
                    }
                });
        typeRegistry.put("IMAGE", type);
        typeRegistry.put("ATTACHMENT", type);
        storageRegistry.put("TEST", (IFileStorageHandler) Proxy.newProxyInstance(IFileStorageHandler.class.getClassLoader(),
                new Class[]{IFileStorageHandler.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getData": return new ByteArrayInputStream(storedData);
                        // 动态代理显式调用接口默认实现，验证已有仅提供流的处理器仍可下载图片。
                        case "getDownloadData": return InvocationHandler.invokeDefault(proxy, method, args);
                        case "saveData":
                            saveAttempts++;
                            beforeSave.run();
                            if (failStorage) { throw new IOException("模拟存储失败"); }
                            storedData = IOUtils.toByteArray((InputStream) args[1]);
                            return "TEST:picture";
                        case "isAllowLocalFallback": return false;
                        default: return null;
                    }
                }));
        fileMapper = (FileMapper) Proxy.newProxyInstance(FileMapper.class.getClassLoader(), new Class[]{FileMapper.class},
                (proxy, method, args) -> {
                    if ("getFileTypeConfigByType".equals(method.getName())) { return null; }
                    if ("insertFile".equals(method.getName()) || "updateFile".equals(method.getName())) {
                        savedFile = (FileVo) args[0];
                        savedFile.setId(100L);
                        return 1;
                    }
                    if ("getFileById".equals(method.getName())) {
                        return savedFile;
                    }
                    throw new AssertionError("不应调用其他附件 Mapper 方法：" + method.getName());
                });
        TenantContext.init();
        UserContext.init((UserContext) null).setUserUuid("image-user");
        TenantContext.init("image-security-test");
    }

    /** 恢复处理器注册表和线程上下文，避免影响其他测试。 */
    @After
    public void cleanup() {
        typeRegistry.clear();
        typeRegistry.putAll(originalTypes);
        storageRegistry.clear();
        storageRegistry.putAll(originalStorage);
        if (UserContext.get() != null) { UserContext.get().release(); }
        TenantContext.get().release();
    }

    /** 构造测试附件记录，固定 ID 避免调用全局 ID 生成器。 */
    protected static FileVo record(String type, String name) {
        FileVo file = new FileVo();
        file.setId(100L);
        file.setType(type);
        file.setName(name);
        file.setPath("TEST:picture");
        file.setUserUuid("image-user");
        return file;
    }

    /** 构造下载参数。 */
    protected static JSONObject query() {
        JSONObject query = new JSONObject();
        query.put("id", 100L);
        return query;
    }

    /** 生成正常图片，GIF 使用两帧以验证动画不被重编码或截断。 */
    protected static byte[] image(String format) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        if ("gif".equals(format)) {
            ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
            try (ImageOutputStream target = ImageIO.createImageOutputStream(output)) {
                writer.setOutput(target);
                writer.prepareWriteSequence(null);
                writer.writeToSequence(new IIOImage(image, null, null), null);
                image.setRGB(0, 0, 0xff0000);
                writer.writeToSequence(new IIOImage(image, null, null), null);
                writer.endWriteSequence();
            } finally {
                writer.dispose();
            }
        } else {
            Assert.assertTrue(ImageIO.write(image, format, output));
        }
        return output.toByteArray();
    }

    /** 通过接口代理提供上传文件，无需启动 Servlet 容器。 */
    protected static MultipartHttpServletRequest request(Upload upload) {
        return (MultipartHttpServletRequest) Proxy.newProxyInstance(MultipartHttpServletRequest.class.getClassLoader(),
                new Class[]{MultipartHttpServletRequest.class}, (proxy, method, args) -> {
                    if ("getFile".equals(method.getName())) {
                        return upload;
                    }
                    return null;
                });
    }

    /** 定位测试所需字段，生产代码不增加注入接口。 */
    protected static Field field(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    /** 在测试中注入 Mapper 替身。 */
    protected static void inject(Object target, String name, Object value) throws Exception {
        field(target.getClass(), name).set(target, value);
    }

    /** 上传文件替身在重复打开时改变内容，以捕获验证后重读原始上传的缺陷。 */
    protected static final class Upload implements MultipartFile {
        protected final String name;
        protected final String mime;
        protected final byte[] bytes;
        protected int readCount;
        protected boolean closed;

        /** 保存当前测试上传的原始内容。 */
        protected Upload(String name, String mime, byte[] bytes) {
            this.name = name;
            this.mime = mime;
            this.bytes = bytes;
        }

        @Override public String getName() { return "upload"; }
        @Override public String getOriginalFilename() { return name; }
        @Override public String getContentType() { return mime; }
        @Override public boolean isEmpty() { return bytes.length == 0; }
        @Override public long getSize() { return bytes.length; }
        @Override public byte[] getBytes() { return bytes.clone(); }

        /** 单次返回正常图片，重复读取返回恶意内容。 */
        @Override
        public InputStream getInputStream() {
            readCount++;
            byte[] content = bytes;
            if (readCount > 1) {
                content = "<script/>".getBytes(StandardCharsets.UTF_8);
            }
            return new ByteArrayInputStream(content) {
                /** 记录输入流关闭行为。 */
                @Override public void close() throws IOException { closed = true; super.close(); }
            };
        }

        /** 当前接口不应依赖 MultipartFile 直接转存。 */
        @Override public void transferTo(File dest) { throw new AssertionError("不应直接转存上传文件"); }
    }

    /** 收集响应头和输出内容，验证失败时没有已提交的图片响应。 */
    protected static final class Response {
        protected final Map<String, String> headers = new HashMap<>();
        protected final ByteArrayOutputStream output = new ByteArrayOutputStream();
        protected String contentType;

        /** 创建最小响应代理，仅实现本次下载需要的契约。 */
        protected HttpServletResponse proxy() {
            return (HttpServletResponse) Proxy.newProxyInstance(HttpServletResponse.class.getClassLoader(),
                    new Class[]{HttpServletResponse.class}, (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "setContentType": contentType = (String) args[0]; return null;
                            case "setHeader": headers.put((String) args[0], (String) args[1]); return null;
                            case "getOutputStream": return new ServletOutputStream() {
                                @Override public boolean isReady() { return true; }
                                @Override public void setWriteListener(WriteListener listener) { }
                                @Override public void write(int value) { output.write(value); }
                            };
                            default: return null;
                        }
                    });
        }
    }
}
