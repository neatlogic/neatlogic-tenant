package neatlogic.module.tenant.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** 使用真实文件和计数读取器验证日志正文、分组顺序及提前停止行为。 */
public class LogFileExportUtilTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /** 按文字规则逐文本验证附件；原人工示例的三处差异保留在测试资源说明中。 */
    @Test
    public void shouldMatchPastedLogsUsingExactTextRules() throws Exception {
        String source = resource("source.log");
        String actual = export(source, null);
        Assert.assertEquals(resource("expected-strict.log"), actual);
        Assert.assertEquals(16, actual.lines().filter(line -> line.startsWith("[ERROR]")).count());
        Assert.assertEquals(5, actual.lines().filter(line -> line.startsWith("[ERROR]"))
                .map(line -> line.split(" ")[3]).distinct().count());
    }

    /** 三部分分别去重，A/B/A 的 A 留在最后一次出现处；不同线程和租户仍属于同组。 */
    @Test
    public void shouldDeduplicateEachPartAcrossWholeGroup() throws Exception {
        String h1 = header(1, "a.Logger[10]", "thread-a", "tenant-a", "");
        String h2 = header(2, "a.Logger[10]", "thread-b", "tenant-b", "/api/test(127.0.0.1)");
        String h3 = header(3, "a.Logger[10]", "thread-c", "tenant-c", "");
        String sqlA = "执行SQL语句 a.Mapper:select 1\n";
        String sqlB = "执行SQL语句 a.Mapper:select 2\n";
        String source = h1 + "{\"a\":1}\n" + sqlA + "message A\n"
                + h2 + "{\"a\":2}\n" + sqlA + "message B\n"
                + h3 + "{\"a\":1}\n" + sqlB + "message A\n";
        Assert.assertEquals(h1 + h2 + "{\"a\":2}\n" + sqlA + "message B\n"
                + h3 + "{\"a\":1}\n" + sqlB + "message A\n", export(source, null));
    }

    /** A1、B1、A2、C1 按最后出现位置输出为 B、A、C，logger 或行号不同均不能合并。 */
    @Test
    public void shouldOrderGroupsByLatestOccurrenceAndKeepLinesSeparate() throws Exception {
        String a1 = header(1, "a.Logger[10]");
        String b = header(2, "b.Logger[10]");
        String a2 = header(3, "a.Logger[10]");
        String otherLine = header(4, "a.Logger[11]");
        Assert.assertEquals(b + "same\n" + a1 + a2 + "same\n" + otherLine + "same\n",
                export(a1 + "same\n" + b + "same\n" + a2 + "same\n" + otherLine + "same\n", null));
    }

    /** 多组交错时逆向首先发现的 B 组最后输出，组内头部正序及唯一正文位置保持不变。 */
    @Test
    public void shouldPutFirstReverseDiscoveredGroupLast() throws Exception {
        String a1 = header(1, "a.Logger[10]");
        String b1 = header(2, "b.Logger[10]");
        String a2 = header(3, "a.Logger[10]");
        String c = header(4, "c.Logger[10]");
        String b2 = header(5, "b.Logger[10]");
        String source = a1 + "A\n" + b1 + "B1\n" + a2 + "A\n" + c + "C\n" + b2 + "B2\n";
        Assert.assertEquals(a1 + a2 + "A\n" + c + "C\n" + b1 + "B1\n" + b2 + "B2\n",
                export(source, null));
    }

    /** 日志时间乱序时仍按文件位置排列分组，不按时间戳排序；组内也维持原始位置顺序。 */
    @Test
    public void shouldOrderByFilePositionInsteadOfTimestamp() throws Exception {
        String a1 = header(8, "a.Logger[10]");
        String b = header(9, "b.Logger[10]");
        String a2 = header(2, "a.Logger[10]");
        Assert.assertEquals(b + "B\n" + a1 + a2 + "A\n",
                export(a1 + "A\n" + b + "B\n" + a2 + "A\n", null));
    }

    /** 时间过滤后仅依据保留下来的组排序，文件前导内容仍先输出。 */
    @Test
    public void shouldOrderRetainedGroupsAfterStartTime() throws Exception {
        String old = header(1, "old.Logger[10]");
        String a1 = header(2, "a.Logger[10]");
        String b = header(3, "b.Logger[10]");
        String a2 = header(4, "a.Logger[10]");
        String prefix = "前导原文\n";
        Assert.assertEquals(prefix + b + "B\n" + a1 + a2 + "A\n",
                export(prefix + old + "old\n" + a1 + "A\n" + b + "B\n" + a2 + "A\n", time(2)));
    }

    /** 请求 JSON 的嵌套、括号字符串及转义引号不会导致 SQL 和堆栈错位。 */
    @Test
    public void shouldParseMultilineJsonWithEscapedCharacters() throws Exception {
        String json = JSON.toJSONString(Map.of("nested", List.of(Map.of("text", "} [ \" \\"))),
                SerializerFeature.PrettyFormat) + "\n";
        String sql = "执行SQL语句 a.Mapper:select '中文'\n";
        String h1 = header(1, "a.Logger[10]");
        String h2 = header(2, "a.Logger[10]");
        Assert.assertEquals(h1 + json + h2 + sql + "message\n\tat frame\n",
                export(h1 + json + sql + "message\n\tat frame\n"
                        + h2 + sql + "message\n\tat frame\n", null));
    }

    /** 参数中出现 @type 时仍按普通 JSON 文本识别，不尝试加载类型或实例化对象。 */
    @Test
    public void shouldValidateJsonWithoutInterpretingTypeMetadata() throws Exception {
        String h1 = header(1, "a.Logger[10]");
        String h2 = header(2, "a.Logger[10]");
        String param = "{\"@type\":\"not.a.RealClass\",\"value\":1}\n";
        String sql = "执行SQL语句 a.Mapper:select 1\n";
        Assert.assertEquals(h1 + param + h2 + sql + "message\n",
                export(h1 + param + sql + "message\n" + h2 + sql + "message\n", null));
    }

    /** 文本比较保留 JSON 排版和堆栈差异，不能因为异常描述相同而丢失不同调用链。 */
    @Test
    public void shouldKeepWhitespaceAndDistinctStacks() throws Exception {
        String h1 = header(1, "a.Logger[10]");
        String h2 = header(2, "a.Logger[10]");
        String first = "{\"a\":1}\nexception\n\tat first\n";
        String second = "{ \"a\":1 }\nexception\n\tat second\n";
        Assert.assertEquals(h1 + first + h2 + second, export(h1 + first + h2 + second, null));
    }

    /** 正文末尾空行也是消息文本的一部分，不能在去重时被 trim 丢失。 */
    @Test
    public void shouldKeepTrailingBlankLineDifferences() throws Exception {
        String h1 = header(1, "a.Logger[10]");
        String h2 = header(2, "a.Logger[10]");
        String source = h1 + "message\n" + h2 + "message\n\n\n";
        Assert.assertEquals(source, export(source, null));
    }

    /** 不完整 JSON 和 JSON 后存在同一行文本时，整段保留并按完整消息去重。 */
    @Test
    public void shouldPreserveAmbiguousBodyWithoutPartialRemoval() throws Exception {
        for (String body : new String[]{"{broken\n执行SQL语句 x:select 1\nmessage\n",
                "{} inline message\n执行SQL语句 x:select 1\n", "[not json]\nmessage\n"}) {
            String h1 = header(1, "a.Logger[10]");
            String h2 = header(2, "a.Logger[10]");
            Assert.assertEquals(h1 + h2 + body, export(h1 + body + h2 + body, null));
        }
    }

    /** 开始时间包含等于边界的记录，较早消息和堆栈整体排除。 */
    @Test
    public void shouldFilterAtInclusiveStartTime() throws Exception {
        String h1 = header(1, "a.Logger[10]");
        String h2 = header(2, "a.Logger[10]");
        String h3 = header(3, "a.Logger[10]");
        String source = h1 + "old\n\tat old\n" + h2 + "same\n" + h3 + "same\n";
        Assert.assertEquals(h2 + h3 + "same\n", export(source, time(2)));
        Assert.assertEquals("", export(source, time(4)));
    }

    /** 读取替身禁止跨越较早头部继续读取，也锁定异步乱序采用立即停止的约定。 */
    @Test
    public void shouldStopReadingImmediatelyAtFirstOlderHeader() throws Exception {
        List<String> reversed = new ArrayList<>(List.of(
                "latest stack", "latest", header(3, "a.Logger[10]").stripTrailing(),
                "old stack", header(1, "a.Logger[10]").stripTrailing()));
        AtomicInteger reads = new AtomicInteger();
        LogFileExportUtil.ExportContent content = LogFileExportUtil.readLogs(() -> {
            int index = reads.getAndIncrement();
            Assert.assertTrue("遇到较早头部后不应再次读取", index < reversed.size());
            return reversed.get(index);
        }, time(2));
        StringWriter writer = new StringWriter();
        content.write(writer);
        Assert.assertEquals(header(3, "a.Logger[10]") + "latest\nlatest stack\n", writer.toString());
        Assert.assertEquals(5, reads.get());
    }

    /** 无论是否提前停止，前导原文都保留；不合法日期不能成为日志记录边界。 */
    @Test
    public void shouldPreserveLeadingContentWithAndWithoutCutoff() throws Exception {
        String prefix = "\uFEFF原始前导文本\n\n"
                + "[ERROR]2026-99-99 00:00:00.000 [t] a.Logger[1] [tenant]  -\n";
        String source = prefix + header(1, "a.Logger[10]") + "old\n"
                + header(3, "a.Logger[10]") + "new\n";
        Assert.assertEquals(source, export(source, null));
        Assert.assertEquals(prefix + header(3, "a.Logger[10]") + "new\n", export(source, time(2)));
        Assert.assertEquals(prefix, export(source, time(4)));
        Assert.assertEquals("plain file\n\n", export("plain file\n\n", time(4)));
    }

    /** 中文跨越多个读取块、长 SQL 和长堆栈不会乱码或按行错误去重。 */
    @Test
    public void shouldReadLongUtf8LinesAcrossBlocks() throws Exception {
        String sql = "执行SQL语句 a.Mapper:select '" + "中文😀".repeat(10000) + "'\n";
        String stack = "exception\n" + "\tat 中文调用栈\n".repeat(10000);
        String h1 = header(1, "a.Logger[10]");
        String h2 = header(2, "a.Logger[10]");
        Assert.assertEquals(h1 + h2 + sql + stack, export(h1 + sql + stack + h2 + sql + stack, null));
    }

    /** 空文件、空正文、BOM、不同换行及末尾无换行均可导出。 */
    @Test
    public void shouldHandleEmptyFilesAndLineEndings() throws Exception {
        Assert.assertEquals("", export("", null));
        String h1 = header(1, "a.Logger[10]");
        String h2 = header(2, "a.Logger[10]");
        Assert.assertEquals(h1 + h2, export(h1 + h2.stripTrailing(), null));
        String source = "\uFEFF" + h1 + "消息\n\tat 调用\n" + h2 + "消息\n\tat 调用";
        String expected = "\uFEFF" + h1 + h2 + "消息\n\tat 调用\n";
        Assert.assertEquals(expected, export(source, null));
        Assert.assertEquals(expected, export(source.replace("\n", "\r\n"), null));
    }

    /** 不同日志级别不影响 logger 分组，保留每次的级别和完整头部。 */
    @Test
    public void shouldKeepLevelIndependentOfGroupKey() throws Exception {
        String h1 = header(1, "a.Logger[10]").replace("[ERROR]", "[WARN ]");
        String h2 = header(2, "a.Logger[10]");
        Assert.assertEquals(h1 + h2 + "body\n", export(h1 + "body\n" + h2 + "body\n", null));
    }

    /** 真实临时文件通过公开服务入口读取，不依赖数据库。 */
    private String export(String source, Long startTime) throws Exception {
        File file = folder.newFile();
        Files.writeString(file.toPath(), source, StandardCharsets.UTF_8);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        LogFileExportUtil.export(file, startTime, output);
        return output.toString(StandardCharsets.UTF_8.name());
    }

    /** 读取从用户附件整理出的固定样例。 */
    private String resource(String name) throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/logexport/" + name)) {
            Assert.assertNotNull(input);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        }
    }

    /** 构造同一服务器时区下的测试时间。 */
    private long time(int second) {
        return LocalDateTime.of(2026, 10, 30, 17, 0, second).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /** 构造缺省线程和租户的日志头。 */
    private String header(int second, String logger) {
        return header(second, logger, "thread", "tenant(system)", "");
    }

    /** 构造符合真实 Logback layout 的完整头部。 */
    private String header(int second, String logger, String thread, String tenant, String url) {
        return String.format("[ERROR]2026-10-30 17:00:%02d.000 [%s] %s [%s] %s -\n", second, thread, logger, tenant, url);
    }
}
