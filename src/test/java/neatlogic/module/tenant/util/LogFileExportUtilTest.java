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
    // 七个不同的栈帧用于核对折叠门槛、首尾帧及省略行数。
    private static final String SEVEN_FRAMES = frames(1, 7);
    private static final String FOLDED_SEVEN = frame(1) + "\t已省略5行重复堆栈\n" + frame(7);

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

    /** 四条不同 IP 的异常保留描述，前三段各保留首尾帧，最新的十六个栈帧完整保留。 */
    @Test
    public void shouldFoldPastedSixteenFrameStacks() throws Exception {
        String actual = export(resource("stack-tail-source.log"), null);
        Assert.assertEquals(resource("stack-tail-expected.log"), actual);
        Assert.assertEquals(4, actual.lines().filter(line -> line.startsWith("[ERROR]")).count());
        Assert.assertEquals(22, actual.lines().filter(line -> line.startsWith("\tat ")).count());
        Assert.assertEquals(3, actual.lines().filter(line -> line.equals("\t已省略14行重复堆栈")).count());
    }

    /** 附件中的四段匹配分别省略 13、8、43、51 行，最新消息连同 SQL 按原文验证。 */
    @Test
    public void shouldMatchPastedMultiBlockExportExactly() throws Exception {
        String source = resource("stack-block-source.log");
        String actual = export(source, null);
        Assert.assertEquals(resource("stack-block-expected.log"), actual);
        Assert.assertTrue(actual.endsWith(source.substring(source.lastIndexOf("[ERROR]"))));
    }

    /** 中部与末尾使用同一门槛，错开起点的七行可折叠，六行始终完整保留。 */
    @Test
    public void shouldApplySevenLineThresholdAtShiftedPositions() throws Exception {
        String a = header(1, "a.Logger[10]");
        String b = header(2, "a.Logger[10]");
        String shortStack = frames(20, 6);
        String latest = b + "new\n" + shortStack + "separator\n" + SEVEN_FRAMES;
        Assert.assertEquals(a + shortStack + "old\n" + FOLDED_SEVEN + latest,
                export(a + shortStack + "old\n" + SEVEN_FRAMES + latest, null));
        Assert.assertEquals(a + FOLDED_SEVEN + "old\n" + shortStack + latest,
                export(a + SEVEN_FRAMES + "old\n" + shortStack + latest, null));
    }

    /** 同一七行前缀在基准中出现两次，较长候选无论在前在后都要胜出。 */
    @Test
    public void shouldChooseLongestMatchAmongRepeatedWindows() throws Exception {
        String a = header(1, "a.Logger[10]");
        String b = header(2, "a.Logger[10]");
        String longStack = frames(1, 10);
        for (String reference : new String[]{SEVEN_FRAMES + "gap\n" + longStack, longStack + "gap\n" + SEVEN_FRAMES}) {
            Assert.assertEquals(a + frame(1) + "\t已省略8行重复堆栈\n" + frame(10) + b + reference,
                    export(a + longStack + b + reference, null));
        }
    }

    /** A 和 B 都只与 C 比较；仅在 A、B 间重复的七行原样保留。 */
    @Test
    public void shouldAlwaysCompareWithLatestCompleteMessage() throws Exception {
        String a = header(1, "a.Logger[10]");
        String b = header(2, "a.Logger[10]");
        String c = header(3, "a.Logger[10]");
        String olderOnly = frames(10, 7);
        String latest = c + "latest\n" + SEVEN_FRAMES;
        Assert.assertEquals(a + FOLDED_SEVEN + olderOnly + b + olderOnly + FOLDED_SEVEN + latest,
                export(a + SEVEN_FRAMES + olderOnly + b + olderOnly + SEVEN_FRAMES + latest, null));
    }

    /** 普通文本、顶层异常及空行会截断堆栈，即便相同的非堆栈行超过七行也不能折叠。 */
    @Test
    public void shouldKeepNonStackLinesAndBreakMatchesAtThem() throws Exception {
        String a = header(1, "a.Logger[10]");
        String b = header(2, "a.Logger[10]");
        for (String separator : new String[]{"ordinary\n", "java.lang.IllegalStateException: same\n", "\n",
                "\tat not-a-frame\n", "at sample.Trace.run(Trace.java:1)\n"}) {
            String block = frames(1, 4) + separator.repeat(7) + frames(5, 3);
            String source = a + "old\n" + block + b + "new\n" + block;
            Assert.assertEquals(source, export(source, null));
        }
    }

    /** 窗口中任一行的缩进、空格或源码行号变化，都不能视为完全匹配。 */
    @Test
    public void shouldCompareStackLinesExactly() throws Exception {
        String a = header(1, "a.Logger[10]");
        String b = header(2, "a.Logger[10]");
        String middle = frame(4);
        for (String different : new String[]{middle.replace("\t", "    "), middle.replace(")\n", ") \n"),
                middle.replace(":4)", ":40)")}) {
            String source = a + SEVEN_FRAMES.replace(middle, different) + b + SEVEN_FRAMES;
            Assert.assertEquals(source, export(source, null));
        }
    }

    /** Caused by、Suppressed 和两种省略帧格式都可参与连续堆栈匹配。 */
    @Test
    public void shouldRecognizeCauseSuppressedAndOmittedFrameLines() throws Exception {
        String a = header(1, "a.Logger[10]");
        String b = header(2, "a.Logger[10]");
        String cause = "Caused by: java.lang.IllegalStateException: cause\n";
        String last = "\t... 3 more\n";
        String stack = cause + frames(1, 2) + "\tSuppressed: java.lang.RuntimeException: suppressed\n"
                + frame(3) + "\t... 2 common frames omitted\n" + last;
        Assert.assertEquals(a + cause + "\t已省略5行重复堆栈\n" + last + b + "latest\n" + stack,
                export(a + stack + b + "latest\n" + stack, null));
    }

    /** 同一消息重复出现时整段去重优先，折叠只发生在其最后一次出现处。 */
    @Test
    public void shouldDeduplicateWholeMessagesBeforeFolding() throws Exception {
        String a = header(1, "a.Logger[10]");
        String b = header(2, "a.Logger[10]");
        String c = header(3, "a.Logger[10]");
        String d = header(4, "a.Logger[10]");
        String latest = "latest\n" + SEVEN_FRAMES;
        Assert.assertEquals(a + b + FOLDED_SEVEN + c + d + latest,
                export(a + SEVEN_FRAMES + b + SEVEN_FRAMES + c + latest + d + latest, null));
    }

    /** 即使存在完整的七行匹配也不能跨 logger 组折叠。 */
    @Test
    public void shouldNotShareMessageReferenceAcrossGroups() throws Exception {
        String a = header(1, "a.Logger[10]");
        String b = header(2, "b.Logger[10]");
        String source = a + "old\n" + SEVEN_FRAMES + b + "new\n" + SEVEN_FRAMES;
        Assert.assertEquals(source, export(source, null));
    }

    /** 最新记录没有消息时向前取基准，非空的普通消息也能成为固定基准。 */
    @Test
    public void shouldUseLastNonEmptyMessageAsFixedReference() throws Exception {
        String a = header(1, "a.Logger[10]");
        String b = header(2, "a.Logger[10]");
        String c = header(3, "a.Logger[10]");
        String latest = b + "new\n" + SEVEN_FRAMES;
        Assert.assertEquals(a + FOLDED_SEVEN + latest + c, export(a + SEVEN_FRAMES + latest + c, null));
        String source = a + SEVEN_FRAMES + latest + c + "ordinary\n";
        Assert.assertEquals(source, export(source, null));
    }

    /** 参数和 SQL 保留各自的原文，只有消息里的重复堆栈参与折叠。 */
    @Test
    public void shouldFoldOnlyMessageAndKeepParameterAndSqlDifferences() throws Exception {
        String a = header(1, "a.Logger[10]") + "{\"ip\":\"old\"}\n执行SQL语句 sample.Mapper:select 1\nold\n";
        String b = header(2, "a.Logger[10]") + "{\"ip\":\"new\"}\n执行SQL语句 sample.Mapper:select 2\nnew\n";
        Assert.assertEquals(a + FOLDED_SEVEN + b + SEVEN_FRAMES, export(a + SEVEN_FRAMES + b + SEVEN_FRAMES, null));
    }

    /** 统一换行后折叠堆栈，真实空白尾行保留，无末尾换行也能识别最后一帧。 */
    @Test
    public void shouldFoldAcrossLineEndingsWithoutDroppingRealBlankLines() throws Exception {
        String a = header(1, "a.Logger[10]") + "old\n";
        String b = header(2, "a.Logger[10]") + "new\n";
        String source = a + SEVEN_FRAMES + b + SEVEN_FRAMES;
        String expected = a + FOLDED_SEVEN + b + SEVEN_FRAMES;
        Assert.assertEquals(expected, export(source.replace("\n", "\r\n"), null));
        Assert.assertEquals(expected, export(source.substring(0, source.length() - 1), null));
        Assert.assertEquals(a + FOLDED_SEVEN + "\n" + b + SEVEN_FRAMES + "\n",
                export(a + SEVEN_FRAMES + "\n" + b + SEVEN_FRAMES + "\n", null));
    }

    /** 时间过滤后较早的保留记录才能折叠，基准消息仍完整输出。 */
    @Test
    public void shouldFoldRetainedMessagesAfterTimeFiltering() throws Exception {
        String a = header(1, "a.Logger[10]") + "old\n";
        String b = header(2, "a.Logger[10]") + "middle\n";
        String c = header(3, "a.Logger[10]") + "latest\n";
        Assert.assertEquals(b + FOLDED_SEVEN + c + SEVEN_FRAMES,
                export(a + SEVEN_FRAMES + b + SEVEN_FRAMES + c + SEVEN_FRAMES, time(2)));
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

    /** 用源码行号标识测试帧，便于核对折叠边界。 */
    private static String frame(int line) {
        return "\tat sample.Trace.run(Trace.java:" + line + ")\n";
    }

    /** 构造可按行号核对的连续堆栈。 */
    private static String frames(int start, int count) {
        StringBuilder stack = new StringBuilder();
        for (int i = start; i < start + count; i++) {
            stack.append(frame(i));
        }
        return stack.toString();
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
