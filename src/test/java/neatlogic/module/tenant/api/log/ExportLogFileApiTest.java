package neatlogic.module.tenant.api.log;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.file.core.FileStorageMediumFactory;
import neatlogic.framework.file.core.IFileStorageHandler;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.binarystream.PrivateBinaryStreamApiComponentBase;
import neatlogic.module.framework.file.handler.LocalFileSystemHandler;
import neatlogic.module.tenant.service.ServerService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;

/** 主接口继承分组、时间、节点及安全回归，并验证原始文件下载的字节兼容性。 */
public class ExportLogFileApiTest extends ExportLogFile2ApiTest {
    private Map<String, IFileStorageHandler> storageRegistry;
    private IFileStorageHandler oldLocalHandler;

    /** 测试目标为原始导出入口，复用兼容入口的完整输出断言。 */
    @Override
    protected PrivateBinaryStreamApiComponentBase createApi() {
        return new ExportLogFileApi();
    }

    /** 主接口的公开路径保持不变。 */
    @Override
    protected String token() {
        return "log/file/export";
    }

    /** 安装生产本地文件处理器，使原始模式也通过真实文件流验证。 */
    @Before
    @SuppressWarnings("unchecked")
    public void setupLocalStorage() throws Exception {
        Field field = FileStorageMediumFactory.class.getDeclaredField("componentMap");
        field.setAccessible(true);
        storageRegistry = (Map<String, IFileStorageHandler>) field.get(null);
        oldLocalHandler = storageRegistry.put("FILE", new LocalFileSystemHandler());
    }

    /** 恢复原文件处理器，不影响其他文件相关测试。 */
    @After
    public void restoreLocalStorage() {
        if (oldLocalHandler == null) {
            storageRegistry.remove("FILE");
        } else {
            storageRegistry.put("FILE", oldLocalHandler);
        }
    }

    /** 缺省、原始及未知类型均保留 BOM、CRLF、重复正文和无末尾换行，并忽略开始时间。 */
    @Test
    public void shouldCopyOriginalBytesForLegacyAndOriginalRequests() throws Exception {
        byte[] original = ("\uFEFF" + header(1) + "重复\n" + header(2) + "重复")
                .replace("\n", "\r\n").getBytes(StandardCharsets.UTF_8);
        Files.write(new File(logDirectory, "error.log").toPath(), original);
        for (String type : new String[]{null, "originalData", "unknownType"}) {
            JSONObject param = params(101, "error.log");
            if (type == null) {
                param.remove("type");
            } else {
                param.put("type", type);
            }
            param.put("startTime", time(59));
            Response response = new Response();
            api.myDoService(param, null, response.proxy());
            Assert.assertArrayEquals(original, response.bytes.toByteArray());
            Assert.assertTrue(response.headers.get("Content-Disposition").contains("127.0.0.1-101-"));
        }
    }

    /** 分组模式开始时间为空时仍对全文件去重，而不是回落为原始下载。 */
    @Test
    public void shouldDeduplicateEntireFileWithoutStartTime() throws Exception {
        Files.writeString(new File(logDirectory, "error.log").toPath(),
                header(1) + "same\n" + header(2) + "same\n");
        Response response = new Response();
        api.myDoService(params(101, "error.log"), null, response.proxy());
        Assert.assertEquals(header(1) + header(2) + "same\n", response.text());
    }

    /** 远程原始下载原样转发类型和时间，最终内容仍不受时间参数影响。 */
    @Test
    public void shouldForwardOriginalModeWithoutFilteringBytes() throws Exception {
        server.setServerId(102);
        byte[] original = (header(1) + "same\r\n" + header(2) + "same").getBytes(StandardCharsets.UTF_8);
        Files.write(new File(logDirectory, "error.log").toPath(), original);
        JSONObject param = params(102, "error.log");
        param.put("type", "originalData");
        param.put("startTime", time(59));
        Response response = new Response();
        PrivateBinaryStreamApiComponentBase target = createApi();
        inject(target, "serverMapper", mapper());
        int[] forwards = {0};
        inject(api, "serverService", Proxy.newProxyInstance(ServerService.class.getClassLoader(),
                new Class<?>[]{ServerService.class}, (proxy, method, args) -> {
                    Assert.assertEquals("downloadOtherServerApi", method.getName());
                    Assert.assertSame(param, args[0]);
                    Assert.assertEquals("originalData", ((JSONObject) args[0]).getString("type"));
                    Assert.assertEquals(Long.valueOf(time(59)), ((JSONObject) args[0]).getLong("startTime"));
                    forwards[0]++;
                    Config.SCHEDULE_SERVER_ID = 102;
                    try {
                        target.myDoService((JSONObject) args[0], null, response.proxy());
                    } finally {
                        Config.SCHEDULE_SERVER_ID = 101;
                    }
                    return null;
                }));
        api.myDoService(param, null, response.proxy());
        Assert.assertEquals(1, forwards[0]);
        Assert.assertArrayEquals(original, response.bytes.toByteArray());
    }

    /** 类型声明为可选字符串，不改变已有调用者的必填参数。 */
    @Test
    public void shouldDeclareOptionalExportType() throws Exception {
        Input input = api.getClass().getMethod("myDoService", JSONObject.class,
                HttpServletRequest.class, HttpServletResponse.class).getAnnotation(Input.class);
        Param type = Arrays.stream(input.value()).filter(param -> "type".equals(param.name())).findFirst().get();
        Assert.assertEquals(ApiParamType.STRING, type.type());
        Assert.assertFalse(type.isRequired());
    }
}
