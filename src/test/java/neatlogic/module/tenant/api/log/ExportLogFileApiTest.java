package neatlogic.module.tenant.api.log;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.constvalue.SystemProperty;
import neatlogic.framework.exception.SystemPropertyNotFoundException;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.exception.file.FileNotFoundException;
import neatlogic.framework.exception.file.FilePathIllegalException;
import neatlogic.framework.exception.server.ServerNotFoundException;
import neatlogic.framework.file.core.FileStorageMediumFactory;
import neatlogic.framework.file.core.IFileStorageHandler;
import neatlogic.framework.heartbeat.dao.mapper.ServerMapper;
import neatlogic.framework.heartbeat.dto.ServerClusterVo;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.util.TimeUtil;
import neatlogic.module.framework.file.handler.LocalFileSystemHandler;
import neatlogic.module.tenant.service.ServerService;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** 使用真实日志处理工具和节点、响应替身，验证主接口的原始下载、分组去重及异常处理。 */
public class ExportLogFileApiTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private final ExportLogFileApi api = new ExportLogFileApi();
    private ServerClusterVo server;
    private File logDirectory;
    private String oldLogHome;
    private int oldServerId;
    private Map<String, IFileStorageHandler> storageRegistry;
    private IFileStorageHandler oldLocalHandler;

    /** 备份环境并注入单节点 Mapper，测试只操作临时目录中的日志。 */
    @Before
    public void setup() throws Exception {
        oldLogHome = System.getProperty(SystemProperty.LOG4J_HOME);
        oldServerId = Config.SCHEDULE_SERVER_ID;
        Config.SCHEDULE_SERVER_ID = 101;
        logDirectory = folder.newFolder("logs");
        System.setProperty(SystemProperty.LOG4J_HOME, logDirectory.getAbsolutePath());
        TenantContext.init("log-export-test");
        server = new ServerClusterVo();
        server.setServerId(101);
        server.setIp("127.0.0.1");
        inject(api, "serverMapper", mapper());
    }

    /** 恢复全局配置和线程上下文，避免改变其他测试的文件根目录。 */
    @After
    public void cleanup() {
        Config.SCHEDULE_SERVER_ID = oldServerId;
        if (oldLogHome == null) {
            System.clearProperty(SystemProperty.LOG4J_HOME);
        } else {
            System.setProperty(SystemProperty.LOG4J_HOME, oldLogHome);
        }
        TenantContext.get().release();
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

    /** 真实工具完成开始时间过滤和去重，保留原有下载名称、内容类型及返回对象。 */
    @Test
    public void shouldDownloadGroupedLocalFileWithStartTime() throws Exception {
        Files.writeString(new File(logDirectory, "error.log").toPath(),
                header(1) + "old\n" + header(2) + "same\n" + header(3) + "same\n", StandardCharsets.UTF_8);
        Response response = new Response();
        JSONObject param = params(101, "error.log");
        param.put("startTime", time(2));
        Assert.assertTrue(((JSONObject) api.myDoService(param, null, response.proxy())).isEmpty());
        Assert.assertEquals(header(2) + header(3) + "same\n", response.text());
        Assert.assertEquals("application/octet-stream", response.contentType);
        Assert.assertEquals(" attachment; filename=\"127.0.0.1-101-" + TimeUtil.yyyymmdd() + "-distinct-error.log\"",
                response.headers.get("Content-Disposition"));
        Assert.assertTrue(response.closed);
    }

    /** 可选开始时间未传时导出全文件，节点没有 IP 时仍保留原有文件名前缀。 */
    @Test
    public void shouldDownloadWholeFileWhenStartTimeAbsent() throws Exception {
        server.setIp(null);
        Files.writeString(new File(logDirectory, "error.log").toPath(), header(1) + "message\n");
        Response response = new Response();
        api.myDoService(params(101, "error.log"), null, response.proxy());
        Assert.assertEquals(header(1) + "message\n", response.text());
        Assert.assertEquals(" attachment; filename=\"101-" + TimeUtil.yyyymmdd() + "-distinct-error.log\"",
                response.headers.get("Content-Disposition"));
    }

    /** 真实分组下载在重复正文原位置输出入参和 SQL 行数说明，并保留最新原文。 */
    @Test
    public void shouldDownloadRepeatedParameterAndSqlNotices() throws Exception {
        String body = "{\n  \"value\":1\n}\n执行SQL语句 a.Mapper:select 1\n";
        Files.writeString(new File(logDirectory, "error.log").toPath(),
                header(1) + body + "old message\n" + header(2) + body + "latest message\n",
                StandardCharsets.UTF_8);
        Response response = new Response();
        api.myDoService(params(101, "error.log"), null, response.proxy());
        Assert.assertEquals(header(1) + "已省略相同请求入参3行\n已省略相同执行SQL语句1行\nold message\n"
                + header(2) + body + "latest message\n", response.text());
    }

    /** 分组下载入口折叠较早消息的七个连续栈帧，保留首尾帧和最新异常全文。 */
    @Test
    public void shouldDownloadFoldedStacksWithOmittedLineCount() throws Exception {
        String frame = "\tat sample.Trace.run(Trace.java:1)\n";
        String stack = frame.repeat(7);
        Files.writeString(new File(logDirectory, "error.log").toPath(),
                header(1) + "old exception\n" + stack + header(2) + "latest exception\n" + stack,
                StandardCharsets.UTF_8);
        Response response = new Response();
        api.myDoService(params(101, "error.log"), null, response.proxy());
        Assert.assertEquals(header(1) + "old exception\n" + frame + "\t已省略5行重复堆栈\n" + frame
                + header(2) + "latest exception\n" + stack, response.text());
    }

    /** 远程节点执行同一个接口，原参数原样传递，入口节点不能再次处理下载结果。 */
    @Test
    public void shouldForwardStartTimeAndProcessOnlyOnTargetNode() throws Exception {
        server.setServerId(102);
        server.setIp("10.0.0.2");
        Files.writeString(new File(logDirectory, "error.log").toPath(),
                header(1) + "old\n" + header(2) + "same\n" + header(3) + "same\n");
        JSONObject param = params(102, "error.log");
        param.put("startTime", time(2));
        Response response = new Response();
        ExportLogFileApi target = new ExportLogFileApi();
        inject(target, "serverMapper", mapper());
        int[] forwards = {0};
        inject(api, "serverService", Proxy.newProxyInstance(ServerService.class.getClassLoader(),
                new Class<?>[]{ServerService.class}, (proxy, method, args) -> {
                    Assert.assertEquals("downloadOtherServerApi", method.getName());
                    Assert.assertSame(param, args[0]);
                    Assert.assertSame(server, args[1]);
                    Assert.assertSame(response.output, args[2]);
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
        Assert.assertEquals(header(2) + header(3) + "same\n", response.text());
        Assert.assertTrue(response.headers.get("Content-Disposition").contains("10.0.0.2-102-"));
    }

    /** 文件不存在、目录和越界路径必须在日志处理和响应写入前拒绝。 */
    @Test
    public void shouldRejectMissingDirectoryAndUnsafePaths() throws Exception {
        Assert.assertThrows(FileNotFoundException.class,
                () -> api.myDoService(params(101, "missing.log"), null, new Response().proxy()));
        Assert.assertThrows(FileNotFoundException.class,
                () -> api.myDoService(params(101, logDirectory.getAbsolutePath()), null, new Response().proxy()));
        File outside = folder.newFile("outside.log");
        for (String path : new String[]{"../outside.log", "%252e%252e%252foutside.log", outside.getAbsolutePath()}) {
            Assert.assertThrows(FilePathIllegalException.class,
                    () -> api.myDoService(params(101, path), null, new Response().proxy()));
        }
    }

    /** 节点不存在或日志根路径缺失时延续现有错误处理。 */
    @Test
    public void shouldPreserveServerAndSystemPropertyErrors() throws Exception {
        Assert.assertThrows(ServerNotFoundException.class,
                () -> api.myDoService(params(999, "error.log"), null, new Response().proxy()));
        System.clearProperty(SystemProperty.LOG4J_HOME);
        Assert.assertThrows(SystemPropertyNotFoundException.class,
                () -> api.myDoService(params(101, "error.log"), null, new Response().proxy()));
    }

    /** 跨节点错误不能被吞掉或误当作下载成功。 */
    @Test
    public void shouldPropagateRemoteDownloadError() throws Exception {
        server.setServerId(102);
        inject(api, "serverService", Proxy.newProxyInstance(ServerService.class.getClassLoader(),
                new Class<?>[]{ServerService.class}, (proxy, method, args) -> "remote download failed"));
        Assert.assertThrows(ApiRuntimeException.class,
                () -> api.myDoService(params(102, "error.log"), null, new Response().proxy()));
    }

    /** 接口路径和管理员权限保持不变，开始时间在接口元数据中为可选 LONG。 */
    @Test
    public void shouldKeepApiContractAndDeclareOptionalStartTime() throws Exception {
        Assert.assertEquals("log/file/export", api.getToken());
        Assert.assertEquals(ADMIN.class, api.getClass().getAnnotation(AuthAction.class).action());
        Input input = api.getClass().getMethod("myDoService", JSONObject.class,
                HttpServletRequest.class, HttpServletResponse.class).getAnnotation(Input.class);
        Param startTime = Arrays.stream(input.value()).filter(param -> "startTime".equals(param.name())).findFirst().get();
        Assert.assertEquals(ApiParamType.LONG, startTime.type());
        Assert.assertFalse(startTime.isRequired());
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
        ExportLogFileApi target = new ExportLogFileApi();
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

    /** 按当前测试节点返回 Mapper 数据，不触碰真实数据库。 */
    private ServerMapper mapper() {
        return (ServerMapper) Proxy.newProxyInstance(ServerMapper.class.getClassLoader(),
                new Class<?>[]{ServerMapper.class}, (proxy, method, args) -> {
                    Assert.assertEquals("getServerLockByServerId", method.getName());
                    return server.getServerId().equals(args[0]) ? server : null;
                });
    }

    /** 为接口注入最小依赖，保持生产字段封装不变。 */
    private void inject(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    /** 构造下载参数；开始时间由各测试按需添加。 */
    private JSONObject params(int serverId, String fileName) {
        JSONObject result = new JSONObject();
        result.put("serverId", serverId);
        result.put("fileName", fileName);
        result.put("type", "groupAndDeduplicate");
        return result;
    }

    /** 构造服务器默认时区的毫秒时间戳。 */
    private long time(int second) {
        return LocalDateTime.of(2026, 10, 30, 17, 0, second).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /** 构造固定 logger 的真实日志头。 */
    private String header(int second) {
        return String.format("[ERROR]2026-10-30 17:00:%02d.000 [thread] a.Logger[10] [tenant(system)]  -\n", second);
    }

    /** 收集响应头和字节流，避免启动 Servlet 容器。 */
    private static class Response {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final Map<String, String> headers = new HashMap<>();
        private String contentType;
        private boolean closed;
        private final ServletOutputStream output = new ServletOutputStream() {
            /** 内存输出始终可写。 */
            @Override
            public boolean isReady() { return true; }
            /** 测试使用同步写入。 */
            @Override
            public void setWriteListener(WriteListener listener) { }
            /** 保存所有输出字节。 */
            @Override
            public void write(int value) { bytes.write(value); }
            /** 记录接口是否关闭响应流。 */
            @Override
            public void close() { closed = true; }
        };

        /** 仅模拟该接口使用的响应方法，额外调用立即使测试失败。 */
        HttpServletResponse proxy() {
            return (HttpServletResponse) Proxy.newProxyInstance(HttpServletResponse.class.getClassLoader(),
                    new Class<?>[]{HttpServletResponse.class}, (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "getOutputStream": return output;
                            case "setHeader": headers.put((String) args[0], (String) args[1]); return null;
                            case "setContentType": contentType = (String) args[0]; return null;
                            default: throw new AssertionError(method.getName());
                        }
                    });
        }

        /** 按导出契约使用 UTF-8 解码响应内容。 */
        String text() {
            return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
