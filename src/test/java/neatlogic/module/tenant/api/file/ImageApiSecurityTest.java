package neatlogic.module.tenant.api.file;

import com.alibaba.fastjson.JSONObject;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.module.tenant.api.user.UserAvatarUploadApi;
import neatlogic.module.tenant.util.ImageFileSnapshot;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;

/** 使用隔离的存储验证图片上传、下载和不可变快照。 */
public class ImageApiSecurityTest extends ImageApiSecurityFixture {
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

    /** 浏览器裁剪默认使用无扩展名的 blob，兼容该名称时仍须拒绝伪造内容及具名格式错配。 */
    @Test
    public void shouldAcceptCropBlobWithoutRelaxingImageValidation() throws Exception {
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
        for (String format : new String[]{"jpeg", "png", "gif"}) {
            byte[] bytes = image(format);
            JSONObject result = (JSONObject) api.myDoService(new JSONObject(), request(new Upload("blob", "text/html", bytes)), null);
            Assert.assertTrue(result.toJSONString(), result.getBooleanValue("uploaded"));
            Assert.assertEquals("avatar." + format, savedFile.getName());
            Assert.assertEquals("image/" + format, savedFile.getContentType());
            Assert.assertEquals("api/binary/image/download?id=100", result.getString("url"));
            Assert.assertArrayEquals(bytes, storedData);
        }
        FileVo lastSavedFile = savedFile;
        for (Upload upload : new Upload[]{
                new Upload("blob", "image/jpeg", "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8)),
                new Upload("blob", "image/png", "<svg xmlns='http://www.w3.org/2000/svg'/>".getBytes(StandardCharsets.UTF_8)),
                new Upload("avatar.jpg", "image/jpeg", image("png"))}) {
            JSONObject result = (JSONObject) api.myDoService(new JSONObject(), request(upload), null);
            Assert.assertFalse(result.toJSONString(), result.getBooleanValue("uploaded"));
            Assert.assertSame(lastSavedFile, savedFile);
        }
        Assert.assertEquals(3, avatarUpdates);
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
        } catch (InvocationTargetException ex) {
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
            api.getClass().getMethod("myDoService", JSONObject.class, HttpServletRequest.class, HttpServletResponse.class)
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
            api.getClass().getMethod("myDoService", JSONObject.class, HttpServletRequest.class, HttpServletResponse.class)
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
                    api.getClass().getMethod("myDoService", JSONObject.class, HttpServletRequest.class, HttpServletResponse.class)
                            .invoke(api, query(), null, response.proxy());
                    Assert.fail("污染图片不应下载成功");
                } catch (InvocationTargetException ex) {
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

}
