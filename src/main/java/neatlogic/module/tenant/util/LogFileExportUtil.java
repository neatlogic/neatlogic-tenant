package neatlogic.module.tenant.util;

import com.alibaba.fastjson.JSONValidator;
import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 将指定文件中的日志逆向读取、按调用位置分组，并分别去重请求参数、SQL 和消息。 */
public final class LogFileExportUtil {
    // 必须匹配完整头部，避免将堆栈、SQL 中的方括号误认为新记录。
    private static final Pattern HEADER_PATTERN = Pattern.compile(
            "^\\[(TRACE|DEBUG|INFO|WARN|ERROR|OFF|ALL) *\\]"
                    + "(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) "
                    + "\\[(.*?)\\] ([^\\s\\[\\]]+\\[(?:\\d+|\\?)\\]) \\[(.*?)\\] (.*?) - *$");
    private static final DateTimeFormatter LOG_TIME = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss.SSS").withResolverStyle(ResolverStyle.STRICT);
    private static final String SQL_PREFIX = "执行SQL语句 ";
    // 仅识别标准 Java 堆栈行，普通消息及顶层异常描述不参与公共尾部压缩。
    private static final Pattern STACK_TRACE_LINE = Pattern.compile(
            "(?:[\\t ]+at[\\t ]+[^\\s()]+\\([^\\r\\n()]*\\)[\\t ]*"
                    + "|[\\t ]*(?:Caused by:|Suppressed:)[\\t ]+\\S[^\\r\\n]*"
                    + "|[\\t ]+\\.\\.\\.[\\t ]+\\d+[\\t ]+(?:common frames omitted|more)[\\t ]*)");
    private static final String OMITTED_STACK_NOTICE = "已省略相同的堆栈行\n";

    /** 无状态工具类仅通过静态方法调用，不注册为 Spring Bean。 */
    private LogFileExportUtil() {
    }

    /** 按服务器时区过滤记录，以 UTF-8 写入输出流；流的生命周期由调用接口管理。 */
    public static void export(File file, Long startTime, OutputStream output) throws IOException {
        ExportContent content;
        try (ReversedLinesFileReader reader = ReversedLinesFileReader.builder()
                .setFile(file).setBufferSize(8192).setCharset(StandardCharsets.UTF_8).get()) {
            content = readLogs(reader::readLine, startTime);
        }
        Writer writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
        if (content.stoppedAtStartTime) {
            // 提前停止时只补读文件前导内容，到首个合法头部即结束，不扫描较早日志。
            writeLeadingContent(file, writer);
        }
        content.write(writer);
        writer.flush();
    }

    /** 逐行恢复一条完整记录；单独保留读取接口便于验证时间边界确实停止后续读取。 */
    static ExportContent readLogs(ReverseLineReader reader, Long startTime) throws IOException {
        ExportContent result = new ExportContent();
        Deque<String> bodyLines = new ArrayDeque<>();
        ZoneId zone = ZoneId.systemDefault();
        String line;
        while ((line = reader.readLine()) != null) {
            Header header = parseHeader(line, zone);
            if (header == null) {
                bodyLines.addFirst(line);
                continue;
            }
            // 此时 bodyLines 已恢复为正序，过滤以整条记录为单位，不留下半段堆栈。
            if (startTime != null && header.timestamp < startTime) {
                result.stoppedAtStartTime = true;
                return result;
            }
            LogGroup group = result.groups.computeIfAbsent(header.logger, key -> new LogGroup());
            group.add(line, joinLines(bodyLines));
            bodyLines.clear();
        }
        // 文件头没有匹配到头部的文本不参与分组或时间过滤。
        result.leadingContent = joinLines(bodyLines);
        return result;
    }

    /** 提前停止后仍保留不带可识别时间的文件前导内容。 */
    private static void writeLeadingContent(File file, Writer writer) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            ZoneId zone = ZoneId.systemDefault();
            while ((line = reader.readLine()) != null && parseHeader(line, zone) == null) {
                writer.write(line);
                writer.write('\n');
            }
        }
    }

    /** 只在解析时忽略 UTF-8 BOM，头部输出仍保留原始文本。 */
    private static Header parseHeader(String line, ZoneId zone) {
        String text = line.startsWith("\uFEFF") ? line.substring(1) : line;
        Matcher matcher = HEADER_PATTERN.matcher(text);
        if (!matcher.matches()) {
            return null;
        }
        try {
            long timestamp = LocalDateTime.parse(matcher.group(2), LOG_TIME).atZone(zone).toInstant().toEpochMilli();
            return new Header(matcher.group(4), timestamp);
        } catch (DateTimeParseException e) {
            // 时间损坏的行按原始正文保留，避免把不可验证的内容当作过滤边界。
            return null;
        }
    }

    /** 读取器已经去除了行结束符，统一为 LF，保留正文中的空行及其他空白。 */
    private static String joinLines(Deque<String> lines) {
        StringBuilder text = new StringBuilder();
        for (String line : lines) {
            text.append(line).append('\n');
        }
        return text.toString();
    }

    /** 依照现有 converter 的输出顺序拆分正文；无法确认的内容整体留在消息中。 */
    private static String[] splitBody(String body) {
        String[] parts = {"", "", ""};
        int offset = 0;
        if (body.startsWith("{") || body.startsWith("[")) {
            int end = findJsonEnd(body);
            if (end < 0) {
                parts[2] = body;
                return parts;
            }
            String json = body.substring(0, end);
            try {
                // 仅校验语法，不反序列化 @type 等字段；比较和输出始终使用原文。
                if (!JSONValidator.from(json).validate()) {
                    parts[2] = body;
                    return parts;
                }
            } catch (RuntimeException e) {
                parts[2] = body;
                return parts;
            }
            int lineEnd = body.indexOf('\n', end);
            if (lineEnd < 0 || !body.substring(end, lineEnd).trim().isEmpty()) {
                parts[2] = body;
                return parts;
            }
            offset = lineEnd + 1;
            parts[0] = body.substring(0, offset);
        }
        if (body.startsWith(SQL_PREFIX, offset)) {
            int lineEnd = body.indexOf('\n', offset);
            if (lineEnd >= 0) {
                parts[1] = body.substring(offset, lineEnd + 1);
                offset = lineEnd + 1;
            }
        }
        parts[2] = body.substring(offset);
        return parts;
    }

    /** 识别完整 JSON 的边界，字符串中的括号、反斜线和转义引号不参与括号计数。 */
    private static int findJsonEnd(String body) {
        Deque<Character> brackets = new ArrayDeque<>();
        boolean quoted = false;
        boolean escaped = false;
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            if (quoted) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    quoted = false;
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == '{' || c == '[') {
                brackets.push(c);
            } else if (c == '}' || c == ']') {
                if (brackets.isEmpty() || brackets.pop() != (c == '}' ? '{' : '[')) {
                    return -1;
                }
                if (brackets.isEmpty()) {
                    return i + 1;
                }
            }
        }
        return -1;
    }

    /** 与固定的完整基准从尾部逐行比较，仅删除连续相同的堆栈行，保留此前的全部原文。 */
    private static String compressCommonStackTail(String message, String reference) {
        // 只排除终止换行产生的虚拟空项；真实空白尾行会因不属于堆栈而阻断比较。
        int messageEnd = message.endsWith("\n") ? message.length() - 1 : message.length();
        int referenceEnd = reference.endsWith("\n") ? reference.length() - 1 : reference.length();
        int retainedEnd = message.length();
        while (messageEnd > 0 && referenceEnd > 0) {
            int messageStart = message.lastIndexOf('\n', messageEnd - 1) + 1;
            int referenceStart = reference.lastIndexOf('\n', referenceEnd - 1) + 1;
            String line = message.substring(messageStart, messageEnd);
            String referenceLine = reference.substring(referenceStart, referenceEnd);
            // 缩进和空格均精确比较，首个不同行或非堆栈行之前的内容不再裁剪。
            if (!line.equals(referenceLine) || !STACK_TRACE_LINE.matcher(line).matches()) {
                break;
            }
            retainedEnd = messageStart;
            messageEnd = Math.max(0, messageStart - 1);
            referenceEnd = Math.max(0, referenceStart - 1);
        }
        if (retainedEnd == message.length()) {
            return message;
        }
        // 实际删除过堆栈时仅补一行说明，前缀保留的换行确保说明与下一条日志头分行。
        return message.substring(0, retainedEnd) + OMITTED_STACK_NOTICE;
    }

    /** 文件读取与单元测试共用的最小读取接口，返回 null 表示到达文件头。 */
    @FunctionalInterface
    interface ReverseLineReader {
        String readLine() throws IOException;
    }

    /** 解析后的分组键和时间，其他头部字段不参与分组。 */
    private static class Header {
        private final String logger;
        private final long timestamp;

        /** 保存解析结果，输出仍使用原始头部。 */
        private Header(String logger, long timestamp) {
            this.logger = logger;
            this.timestamp = timestamp;
        }
    }

    /** 保存头部与唯一正文，不累计重复的异常堆栈。 */
    static class ExportContent {
        // 插入顺序是各组在逆向读取时首次出现的顺序，对应原文件中各组最后出现的位置。
        private final Map<String, LogGroup> groups = new LinkedHashMap<>();
        private String leadingContent = "";
        private boolean stoppedAtStartTime;

        /** 反转分组发现顺序，使原文件中最后出现的组排在末尾，组内仍按原文件正序输出。 */
        void write(Writer writer) throws IOException {
            writer.write(leadingContent);
            List<LogGroup> orderedGroups = new ArrayList<>(groups.values());
            Collections.reverse(orderedGroups);
            for (LogGroup group : orderedGroups) {
                for (LogRecord record : group.records) {
                    writer.write(record.header);
                    writer.write('\n');
                    for (String part : record.parts) {
                        writer.write(part);
                    }
                }
            }
        }
    }

    /** 每部分有独立的去重集合，线程、租户、URL 均不影响同一调用位置的分组。 */
    private static class LogGroup {
        private final Deque<LogRecord> records = new ArrayDeque<>();
        private final List<Set<String>> seenParts = List.of(new HashSet<>(), new HashSet<>(), new HashSet<>());
        // 逆向首次遇到的非空消息就是同组最新消息，始终保留完整原文作为固定基准。
        private String latestMessage;

        /** 逆向首次看到某段正文即为它最后一次出现的位置，正文只挂载在此处。 */
        private void add(String header, String body) {
            String[] parts = splitBody(body);
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].isEmpty() || !seenParts.get(i).add(parts[i])) {
                    parts[i] = "";
                }
            }
            if (!parts[2].isEmpty()) {
                if (latestMessage == null) {
                    latestMessage = parts[2];
                } else {
                    // 整段消息去重仍基于裁剪前原文；仅对需要输出的较早消息压缩尾部。
                    parts[2] = compressCommonStackTail(parts[2], latestMessage);
                }
            }
            records.addFirst(new LogRecord(header, parts));
        }
    }

    /** 每次发生均保存原始头部，正文仅引用该位置应输出的唯一部分。 */
    private static class LogRecord {
        private final String header;
        private final String[] parts;

        /** 保存单条记录的输出信息，重复部分已替换为空字符串。 */
        private LogRecord(String header, String[] parts) {
            this.header = header;
            this.parts = parts;
        }
    }
}
