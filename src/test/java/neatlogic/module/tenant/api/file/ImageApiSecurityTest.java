package neatlogic.module.tenant.api.file;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.file.core.FileStorageMediumFactory;
import neatlogic.framework.file.core.FileTypeHandlerFactory;
import neatlogic.framework.file.core.IFileStorageHandler;
import neatlogic.framework.file.core.IFileTypeHandler;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.module.tenant.api.user.UserAvatarUploadApi;
import neatlogic.module.tenant.util.ImageFileSnapshot;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/** 使用内存存储和请求替身验证图片入口，避免连接数据库或修改真实附件。 */
public class ImageApiSecurityTest {
    private Map<String, IFileTypeHandler> typeRegistry;
    private Map<String, IFileTypeHandler> originalTypes;
    private Map<String, IFileStorageHandler> storageRegistry;
    private Map<String, IFileStorageHandler> originalStorage;
    private FileMapper fileMapper;
    private FileVo savedFile;
    private byte[] storedData;
    private int avatarUpdates;

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
                        case "saveData":
                            storedData = IOUtils.toByteArray((InputStream) args[1]);
                            return "TEST:picture";
                        case "isAllowLocalFallback": return false;
                        default: return null;
                    }
                }));
        fileMapper = (FileMapper) Proxy.newProxyInstance(FileMapper.class.getClassLoader(), new Class[]{FileMapper.class},
                (proxy, method, args) -> {
                    if ("insertFile".equals(method.getName())) {
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
        UserContext.get().release();
        TenantContext.get().release();
    }

    /** 客户端 MIME 不可信，正常格式仍应保存原始内容并返回原编辑器契约。 */
    @Test
    public void shouldUploadSupportedPicturesUsingConfirmedMime() throws Exception {
        for (String format : new String[]{"png", "jpeg", "gif"}) {
            byte[] bytes = image(format);
            Upload upload = new Upload("picture." + format, "text/html", bytes);
            UploadImageApi api = new UploadImageApi();
            inject(api, "fileMapper", fileMapper);
            JSONObject result = (JSONObject) api.myDoService(new JSONObject(), request(upload), null);
            Assert.assertTrue(result.toJSONString(), result.getBooleanValue("uploaded"));
            Assert.assertEquals("api/binary/image/download?id=100", result.getString("url"));
            Assert.assertEquals("image/" + format, savedFile.getContentType());
            Assert.assertArrayEquals(bytes, storedData);
            Assert.assertEquals(1, upload.readCount);
            Assert.assertTrue(upload.closed);
        }
    }

    /** 伪装图片、错扩展名和 SVG 都必须在存储前失败，维持 uploaded/error 返回格式。 */
    @Test
    public void shouldRejectSpoofedUploadsBeforeSaving() throws Exception {
        Upload[] uploads = {
                new Upload("picture.png", "image/png", "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8)),
                new Upload("picture.jpg", "image/jpeg", image("png")),
                new Upload("picture.svg", "image/svg+xml", "<svg xmlns='http://www.w3.org/2000/svg'/>".getBytes(StandardCharsets.UTF_8))
        };
        for (Upload upload : uploads) {
            UploadImageApi api = new UploadImageApi();
            inject(api, "fileMapper", fileMapper);
            JSONObject result = (JSONObject) api.myDoService(new JSONObject(), request(upload), null);
            Assert.assertFalse(result.getBooleanValue("uploaded"));
            Assert.assertTrue(result.containsKey("error"));
            Assert.assertNull(storedData);
            Assert.assertTrue(upload.closed);
        }
    }

    /** 头像与普通图片复用同一校验，拒绝假图时不更新用户信息。 */
    @Test
    public void shouldValidateAvatarAndPreserveItsResponse() throws Exception {
        UserAvatarUploadApi api = new UserAvatarUploadApi();
        inject(api, "fileMapper", fileMapper);
        UserVo user = new UserVo();
        UserMapper userMapper = (UserMapper) Proxy.newProxyInstance(UserMapper.class.getClassLoader(), new Class[]{UserMapper.class},
                (proxy, method, args) -> {
                    if ("getUserByUuid".equals(method.getName())) {
                        return user;
                    }
                    if ("updateUserInfo".equals(method.getName())) {
                        avatarUpdates++;
                        return 1;
                    }
                    throw new AssertionError(method.getName());
                });
        inject(api, "userMapper", userMapper);
        JSONObject rejected = (JSONObject) api.myDoService(new JSONObject(), request(new Upload("bad.png", "image/png", new byte[]{1, 2})), null);
        Assert.assertFalse(rejected.getBooleanValue("uploaded"));
        Assert.assertEquals(0, avatarUpdates);
        JSONObject accepted = (JSONObject) api.myDoService(new JSONObject(), request(new Upload("avatar.png", null, image("png"))), null);
        Assert.assertTrue(accepted.toJSONString(), accepted.getBooleanValue("uploaded"));
        Assert.assertEquals("api/binary/image/download?id=100", accepted.getString("url"));
        Assert.assertEquals(1, avatarUpdates);
        Assert.assertEquals(accepted.getString("url"), JSONObject.parseObject(user.getUserInfo()).getString("avatar"));
    }

    /** 通用上传保存方法也验证图片，其他附件仍按原 MIME 和原始内容保存。 */
    @Test
    public void shouldProtectGenericUploadWithoutChangingAttachments() throws Exception {
        UploadFileApi api = new UploadFileApi();
        Method save = UploadFileApi.class.getDeclaredMethod("saveUploadedData", String.class, MultipartFile.class, FileVo.class);
        save.setAccessible(true);
        FileVo file = record("image", "picture.png");
        Upload upload = new Upload("picture.png", "text/html", image("png"));
        Assert.assertEquals("TEST:picture", save.invoke(api, "image-security-test", upload, file));
        Assert.assertEquals("image/png", file.getContentType());
        Assert.assertEquals(1, upload.readCount);
        storedData = null;
        try {
            save.invoke(api, "image-security-test", new Upload("picture.png", "image/png", new byte[]{1, 2}), file);
            Assert.fail("通用上传不应保存假图");
        } catch (java.lang.reflect.InvocationTargetException ex) {
            Assert.assertTrue(ex.getCause() instanceof IOException);
        }
        Assert.assertNull(storedData);
        file = record("attachment", "source.txt");
        file.setContentType("text/plain");
        byte[] text = "attachment".getBytes(StandardCharsets.UTF_8);
        save.invoke(api, "image-security-test", new Upload("source.txt", "text/plain", text), file);
        Assert.assertArrayEquals(text, storedData);
        Assert.assertEquals("text/plain", file.getContentType());
    }

    /** 历史 MIME 污染应由实际格式纠正，专用和通用下载的输出必须一致。 */
    @Test
    public void shouldConfirmMimeOnBothDownloadEntrypoints() throws Exception {
        savedFile = record("image", "picture.png");
        savedFile.setContentType("text/html");
        storedData = image("png");
        for (Object api : new Object[]{new DownloadImageApi(), new DownloadFileApi()}) {
            inject(api, "fileMapper", fileMapper);
            Response response = new Response();
            api.getClass().getMethod("myDoService", JSONObject.class, javax.servlet.http.HttpServletRequest.class, HttpServletResponse.class)
                    .invoke(api, query(), null, response.proxy());
            Assert.assertEquals("image/png", response.contentType);
            Assert.assertEquals("nosniff", response.headers.get("X-Content-Type-Options"));
            Assert.assertArrayEquals(storedData, response.output.toByteArray());
        }
    }

    /** 历史文件扩展名错误但内容确为支持的图片时仍可下载，并纠正响应媒体类型。 */
    @Test
    public void shouldDownloadHistoricalPicturesWithMismatchedFilename() throws Exception {
        savedFile = record("image", "misnamed.png");
        savedFile.setContentType("image/png");
        storedData = image("jpeg");
        for (Object api : new Object[]{new DownloadImageApi(), new DownloadFileApi()}) {
            inject(api, "fileMapper", fileMapper);
            Response response = new Response();
            api.getClass().getMethod("myDoService", JSONObject.class, javax.servlet.http.HttpServletRequest.class, HttpServletResponse.class)
                    .invoke(api, query(), null, response.proxy());
            Assert.assertEquals("image/jpeg", response.contentType);
            Assert.assertEquals("nosniff", response.headers.get("X-Content-Type-Options"));
            Assert.assertArrayEquals(storedData, response.output.toByteArray());
        }
    }

    /** 历史图片记录中的 HTML 和 SVG 内容在两个下载入口都不能输出任何字节。 */
    @Test
    public void shouldRejectPollutedHistoricalRecordsBeforeWritingResponse() throws Exception {
        savedFile = record("image", "picture.png");
        for (String content : new String[]{"<script>alert(1)</script>", "<svg xmlns='http://www.w3.org/2000/svg'/>"}) {
            storedData = content.getBytes(StandardCharsets.UTF_8);
            for (Object api : new Object[]{new DownloadImageApi(), new DownloadFileApi()}) {
                inject(api, "fileMapper", fileMapper);
                Response response = new Response();
                try {
                    api.getClass().getMethod("myDoService", JSONObject.class, javax.servlet.http.HttpServletRequest.class, HttpServletResponse.class)
                            .invoke(api, query(), null, response.proxy());
                    Assert.fail("污染图片不应下载成功");
                } catch (java.lang.reflect.InvocationTargetException ex) {
                    Assert.assertTrue(ex.getCause() instanceof IOException);
                }
                Assert.assertNull(response.contentType);
                Assert.assertEquals(0, response.output.size());
            }
        }
    }

    /** 在线文档图片维持匿名入口，非图片条目和伪装文件都不得输出。 */
    @Test
    public void shouldOnlyDownloadValidatedClasspathPictures() throws Exception {
        Path root = Files.createTempDirectory("tenant-classpath-image-");
        ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
        Field dataHome = field(Config.class, "DATA_HOME");
        Object originalDataHome = dataHome.get(null);
        dataHome.set(null, root.toString());
        String prefix = "neatlogic/resources/image-security-test/documentonline/";
        Path resources = root.resolve(prefix);
        Files.createDirectories(resources);
        byte[] bytes = image("png");
        Files.write(resources.resolve("picture.png"), bytes);
        Files.write(resources.resolve("wrong-extension.png"), image("jpeg"));
        Files.write(resources.resolve("spoofed.png"), "<script/>".getBytes(StandardCharsets.UTF_8));
        Files.write(resources.resolve("note.md"), "document".getBytes(StandardCharsets.UTF_8));
        try (URLClassLoader loader = new URLClassLoader(new URL[]{root.toUri().toURL()}, originalLoader)) {
            Thread.currentThread().setContextClassLoader(loader);
            DownloadClasspathImageApi api = new DownloadClasspathImageApi();
            Assert.assertEquals(neatlogic.framework.restful.constvalue.ApiAnonymousAccessSupportEnum.ANONYMOUS_ACCESS_WITHOUT_ENCRYPTION,
                    api.supportAnonymousAccess());
            JSONObject query = new JSONObject();
            query.put("filePath", prefix + "picture.png");
            Response response = new Response();
            api.myDoService(query, null, response.proxy());
            Assert.assertEquals("image/png", response.contentType);
            Assert.assertEquals("nosniff", response.headers.get("X-Content-Type-Options"));
            Assert.assertArrayEquals(bytes, response.output.toByteArray());
            query.put("filePath", prefix + "wrong-extension.png");
            Response mismatched = new Response();
            api.myDoService(query, null, mismatched.proxy());
            Assert.assertEquals("image/jpeg", mismatched.contentType);
            Assert.assertArrayEquals(image("jpeg"), mismatched.output.toByteArray());
            for (String name : new String[]{"spoofed.png", "note.md"}) {
                query.put("filePath", prefix + name);
                Response rejected = new Response();
                try {
                    api.myDoService(query, null, rejected.proxy());
                    Assert.fail("非图片资源不应输出");
                } catch (IOException expected) {
                    Assert.assertEquals(0, rejected.output.size());
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(originalLoader);
            dataHome.set(null, originalDataHome);
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }

    /** 快照验证和输出使用相同内容，正常关闭后清理磁盘缓存。 */
    @Test
    public void shouldKeepSnapshotImmutableAndDeleteItAfterUse() throws Exception {
        byte[] original = image("png");
        ImageFileSnapshot snapshot = ImageFileSnapshot.create(new ByteArrayInputStream(original), "picture.png");
        Path path = (Path) field(ImageFileSnapshot.class, "path").get(snapshot);
        try (ImageFileSnapshot image = snapshot; InputStream input = image.openStream()) {
            Assert.assertTrue(Files.exists(path));
            Assert.assertArrayEquals(original, IOUtils.toByteArray(input));
        }
        Assert.assertFalse(Files.exists(path));
    }

    /** 客户端写出失败时也必须清理图片临时快照。 */
    @Test
    public void shouldDeleteSnapshotWhenResponseWriteFails() throws Exception {
        ImageFileSnapshot snapshot = ImageFileSnapshot.create(new ByteArrayInputStream(image("png")), "picture.png");
        Path path = (Path) field(ImageFileSnapshot.class, "path").get(snapshot);
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(HttpServletResponse.class.getClassLoader(),
                new Class[]{HttpServletResponse.class}, (proxy, method, args) -> {
                    if ("getOutputStream".equals(method.getName())) {
                        throw new IOException("模拟客户端连接断开");
                    }
                    return null;
                });
        try (ImageFileSnapshot image = snapshot) {
            image.writeTo(response);
            Assert.fail("应报告写出失败");
        } catch (IOException expected) {
            Assert.assertFalse(Files.exists(path));
        }
    }

    /** 构造测试附件记录，固定 ID 避免调用全局 ID 生成器。 */
    private static FileVo record(String type, String name) {
        FileVo file = new FileVo();
        file.setId(100L);
        file.setType(type);
        file.setName(name);
        file.setPath("TEST:picture");
        file.setUserUuid("image-user");
        return file;
    }

    /** 构造下载参数。 */
    private static JSONObject query() {
        JSONObject query = new JSONObject();
        query.put("id", 100L);
        return query;
    }

    /** 生成正常图片，GIF 使用两帧以验证动画不被重编码或截断。 */
    private static byte[] image(String format) throws IOException {
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
    private static MultipartHttpServletRequest request(Upload upload) {
        return (MultipartHttpServletRequest) Proxy.newProxyInstance(MultipartHttpServletRequest.class.getClassLoader(),
                new Class[]{MultipartHttpServletRequest.class}, (proxy, method, args) -> {
                    if ("getFile".equals(method.getName())) {
                        return upload;
                    }
                    return null;
                });
    }

    /** 定位测试所需字段，生产代码不增加注入接口。 */
    private static Field field(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    /** 在测试中注入 Mapper 替身。 */
    private static void inject(Object target, String name, Object value) throws Exception {
        field(target.getClass(), name).set(target, value);
    }

    /** 上传文件替身在重复打开时改变内容，以捕获验证后重读原始上传的缺陷。 */
    private static final class Upload implements MultipartFile {
        private final String name;
        private final String mime;
        private final byte[] bytes;
        private int readCount;
        private boolean closed;

        /** 保存当前测试上传的原始内容。 */
        private Upload(String name, String mime, byte[] bytes) {
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
    private static final class Response {
        private final Map<String, String> headers = new HashMap<>();
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();
        private String contentType;

        /** 创建最小响应代理，仅实现本次下载需要的契约。 */
        private HttpServletResponse proxy() {
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
