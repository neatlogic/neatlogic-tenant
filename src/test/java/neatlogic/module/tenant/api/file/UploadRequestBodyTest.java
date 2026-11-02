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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.common.util.ModuleUtil;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.module.ModuleVo;
import neatlogic.framework.file.core.FileTypeHandlerFactory;
import neatlogic.framework.file.dto.FileTypeVo;
import neatlogic.framework.importexport.core.ImportExportHandler;
import neatlogic.framework.importexport.core.ImportExportHandlerFactory;
import neatlogic.framework.importexport.dto.ImportExportVo;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.core.privateapi.binarystream.PrivateBinaryStreamApiComponentBase;
import neatlogic.module.tenant.api.importexport.ImportApi;
import neatlogic.module.tenant.api.user.UserAvatarUploadApi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import static org.junit.Assert.*;

/** 真实认证、multipart、Dispatcher、业务模板和上传 API 连续执行；仅存储、Mapper 与导入领域处理器隔离。 */
@RunWith(Parameterized.class)
public class UploadRequestBodyTest extends UploadRequestBodyFixture {
    @Parameterized.Parameter public String authentication;
    @Parameterized.Parameter(1) public String scenario;
    @Parameterized.Parameter(2) public boolean rejected;
    private final ImageApiSecurityFixture storage = new ImageApiSecurityFixture();
    private Set<Path> snapshotsBefore;
    private int snapshotsDuringSave;
    private int imported;
    private Object businessResult;
    private int importStreamOpens;

    /** 上传种类与认证逐项组合，拒绝路径也使用同一入口。 */
    @Parameterized.Parameters(name = "{0}-{1}-rejected={2}")
    public static Collection<Object[]> scenarios() {
        Collection<Object[]> result = new ArrayList<>();
        for (String auth : new String[]{"JWT", "JWT_HEADER", "HMAC", "BEARER", "BASIC"}) {
            for (String scene : new String[]{"attachment", "image", "generic-image", "avatar", "blob", "import"}) {
                for (boolean rejected : new boolean[]{false, true}) { result.add(new Object[]{auth, scene, rejected}); }
            }
        }
        return result;
    }

    /** 复用既有图片安全测试存储；注册临时模块和文件类型，不依赖运行中的租户。 */
    @Before public void installStorage() throws Exception {
        storage.setup();
        snapshotsBefore = imageSnapshots();
        storage.beforeSave = () -> {
            Set<Path> current = imageSnapshots(); current.removeAll(snapshotsBefore);
            snapshotsDuringSave = current.size();
            assertEquals(1, directory.getRoot().list().length);
        };
        put(map(TenantContext.class, "tenantModuleGroupListMap"), TENANT, new ArrayList<>(Collections.singletonList("framework")));
        ModuleVo module = new ModuleVo(); module.setId("request-body-test-module"); module.setGroup("framework");
        addToList(ModuleUtil.class, "moduleList", module);
        for (String type : new String[]{"image", "attachment"}) {
            FileTypeVo entry = new FileTypeVo(); entry.setName(type); entry.setModuleId(module.getId());
            addToList(FileTypeHandlerFactory.class, "fileTypeList", entry);
        }
        setStatic(ImportExportHandlerFactory.class, "fileMapper", storage.fileMapper);
        put(map(ImportExportHandlerFactory.class, "componentMap"), "file", proxy(ImportExportHandler.class,
                (p, m, a) -> m.getName().equals("importData") ? ((ImportExportVo) a[0]).getPrimaryKey() : fallback(m.getReturnType())));
        put(map(ImportExportHandlerFactory.class, "componentMap"), "body-test-object", proxy(ImportExportHandler.class, (p, m, a) -> {
            if (m.getName().equals("checkImportAuth")) { return true; }
            if (m.getName().equals("importData")) {
                ImportExportVo value = (ImportExportVo) a[0];
                assertEquals("完整包内容", value.getData().getString("value"));
                imported++;
                return value.getPrimaryKey();
            }
            return fallback(m.getReturnType());
        }));
    }

    /** 业务快照和请求级临时文件均应清理，存储替身不能污染后续用例。 */
    @After public void restoreStorage() {
        try {
            if (snapshotsBefore != null) {
                Set<Path> current = imageSnapshots(); current.removeAll(snapshotsBefore); assertTrue(current.toString(), current.isEmpty());
            }
        } finally { storage.cleanup(); }
    }

    /** 通过 API 模板调用真实实现，校验最终附件字节、图片快照、头像更新以及导入服务多次打开同一 ZIP。 */
    @Test public void uploadTraversesBusinessAndCleansResources() throws Exception {
        JSONObject fields = new JSONObject(); fields.put("param", "upload");
        fields.put("type", scenario.equals("attachment") ? "attachment" : "image");
        fields.put("targetType", rejected && scenario.equals("import") ? "wrong-type" : "body-test-object");
        PrivateBinaryStreamApiComponentBase api;
        byte[] content;
        String filename;
        switch (scenario) {
            case "attachment":
                api = new UploadFileApi(); content = "附件原始内容".repeat(50).getBytes(StandardCharsets.UTF_8); filename = "source.txt";
                storage.failStorage = rejected; break;
            case "generic-image":
                api = new UploadFileApi(); content = rejected ? new byte[200] : storage.image("png"); filename = "picture.png"; break;
            case "avatar": case "blob":
                api = new UserAvatarUploadApi(); content = rejected ? new byte[200] : storage.image("png"); filename = scenario.equals("blob") ? "blob" : "avatar.png";
                field(api, "userMapper", proxy(UserMapper.class, (p, m, a) -> {
                    if (m.getName().equals("getUserByUuid")) { return user; }
                    if (m.getName().equals("updateUserInfo")) { storage.avatarUpdates++; return 1; }
                    return fallback(m.getReturnType());
                })); break;
            case "import": api = new ImportApi(); content = importPackage(); filename = "package.zip"; break;
            default: api = new UploadImageApi(); content = rejected ? new byte[200] : storage.image("png"); filename = "picture.png";
        }
        if (!scenario.equals("import")) { field(api, "fileMapper", storage.fileMapper); }
        business = (parameters, request, response) -> {
            if (scenario.equals("import")) {
                MultipartHttpServletRequest parsed = (MultipartHttpServletRequest) request;
                Map<String, MultipartFile> files = new HashMap<>();
                for (Map.Entry<String, MultipartFile> entry : parsed.getFileMap().entrySet()) {
                    files.put(entry.getKey(), proxy(MultipartFile.class, (p, m, a) -> {
                        if (m.getName().equals("getInputStream")) { importStreamOpens++; }
                        return m.invoke(entry.getValue(), a);
                    }));
                }
                // 仅计数，所有输入流仍来自同一真实 CommonsMultipartFile，不替换文件内容或解析过程。
                request = proxy(MultipartHttpServletRequest.class, (p, m, a) -> m.getName().equals("getFileMap") ? files : m.invoke(parsed, a));
            }
            businessResult = api.doService(PrivateApiComponentFactory.getApiByToken(TOKEN), parameters, request, response);
            return businessResult;
        };
        omitJwtCookie = !authentication.equals("JWT");
        MockHttpServletRequestBuilder request = uploadRequest().contentType("multipart/form-data; boundary=business-test")
                .content(multipart(fields, filename, content));
        if (authentication.equals("HMAC")) {
            request.header("AuthType", "hmac").header("x-access-key", "tester").header("Authorization",
                    signature(new MockHttpServletRequest("POST", "/api/binary/" + TOKEN), fields.toJSONString()));
        } else if (authentication.equals("JWT_HEADER")) {
            request.header("Authorization", jwtHeader);
        } else if (authentication.equals("BASIC")) {
            request.header("AuthType", "basic").header("Authorization", "Basic " + Base64.getEncoder().encodeToString("tester:test-password".getBytes(StandardCharsets.UTF_8)));
        } else if (authentication.equals("BEARER")) {
            request.header("AuthType", "bearertoken").header("Authorization", "Bearer opaque-test-token");
        }
        MvcResult result = mvc.perform(request).andReturn();
        assertEquals(1, calls); assertFalse(cached); assertEquals(1, temporaryFiles);
        assertEquals(content.length, temporaryBytes); assertEquals(0, directory.getRoot().list().length);
        if (rejected) {
            assertNull(storage.storedData); assertEquals(0, storage.avatarUpdates); assertEquals(0, imported);
            assertEquals(scenario.equals("attachment") ? 1 : 0, storage.saveAttempts);
            if (scenario.equals("image") || scenario.equals("avatar") || scenario.equals("blob")) {
                assertFalse(((JSONObject) businessResult).getBooleanValue("uploaded"));
            } else { assertTrue(result.getResponse().getContentAsString(), result.getResponse().getStatus() >= 400); }
        } else {
            assertEquals(result.getResponse().getContentAsString(), 200, result.getResponse().getStatus());
            if (scenario.equals("import")) {
                assertEquals(1, imported); assertEquals(4, importStreamOpens);
                assertArrayEquals("包内附件原始字节".getBytes(StandardCharsets.UTF_8), storage.storedData);
                assertEquals(1, storage.saveAttempts); assertEquals(0, snapshotsDuringSave);
            }
            else {
                assertArrayEquals(content, storage.storedData);
                assertEquals(scenario.equals("attachment") ? 0 : 1, snapshotsDuringSave);
                if (!scenario.equals("attachment")) { assertEquals("image/png", storage.savedFile.getContentType()); }
                assertEquals(scenario.equals("avatar") || scenario.equals("blob") ? 1 : 0, storage.avatarUpdates);
            }
        }
    }

    /** 生成合法 ZIP，实际服务会执行预检查、对象索引、导入和附件遍历的多次读流。 */
    private static byte[] importPackage() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry("object.json"));
            zip.write("{\"type\":\"body-test-object\",\"primaryKey\":1,\"name\":\"test\",\"data\":{\"value\":\"完整包内容\"}}".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("dependency-folder/7.json"));
            zip.write("{\"type\":\"file\",\"primaryKey\":7,\"name\":\"file\",\"data\":{\"id\":7,\"name\":\"source.txt\",\"type\":\"attachment\"}}".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("attachment-folder/7/source.txt"));
            zip.write("包内附件原始字节".getBytes(StandardCharsets.UTF_8)); zip.closeEntry();
        }
        return bytes.toByteArray();
    }

    /** 发送真实 multipart 字节，禁止用 MockMultipartFile 跳过生产解析器。 */
    private static byte[] multipart(JSONObject fields, String filename, byte[] content) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (String key : fields.keySet()) {
            out.write(("--business-test\r\nContent-Disposition: form-data; name=\"" + key + "\"\r\n\r\n" + fields.getString(key) + "\r\n").getBytes(StandardCharsets.UTF_8));
        }
        out.write(("--business-test\r\nContent-Disposition: form-data; name=\"upload\"; filename=\"" + filename + "\"\r\nContent-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(content); out.write("\r\n--business-test--\r\n".getBytes(StandardCharsets.UTF_8)); return out.toByteArray();
    }

    /** 只观察图片业务快照，已有文件不计入断言也不删除。 */
    private static Set<Path> imageSnapshots() {
        try (Stream<Path> paths = Files.list(Path.of(System.getProperty("java.io.tmpdir")))) {
            return paths.filter(p -> p.getFileName().toString().startsWith("neatlogic-image-")).collect(Collectors.toSet());
        } catch (IOException ex) {
            LoggerFactory.getLogger(UploadRequestBodyTest.class).error("读取测试快照目录失败", ex); throw new AssertionError(ex);
        }
    }

    /** 共享注册表只添加本次测试对象，测试结束时按对象移除。 */
    @SuppressWarnings("unchecked") private <T> void addToList(Class<?> owner, String name, T value) throws Exception {
        Field field = owner.getDeclaredField(name); field.setAccessible(true);
        List<T> values = (List<T>) field.get(null); values.add(value); restore.add(() -> values.remove(value));
    }
}
