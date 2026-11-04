package neatlogic.module.tenant.util;

import com.alibaba.fastjson.JSONValidator;
import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
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
import java.util.HashMap;
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
    // 仅识别标准 Java 堆栈行，普通消息及顶层异常描述会截断匹配段。
    private static final Pattern STACK_TRACE_LINE = Pattern.compile(
            "(?:[\\t ]+at[\\t ]+[^\\s()]+\\([^\\r\\n()]*\\)[\\t ]*"
                    + "|[\\t ]*(?:Caused by:|Suppressed:)[\\t ]+\\S[^\\r\\n]*"
                    + "|[\\t ]+\\.\\.\\.[\\t ]+\\d+[\\t ]+(?:common frames omitted|more)[\\t ]*)");
    private static final int MIN_STACK_MATCH = 7;

    /** 无状态工具类仅通过静态方法调用，不注册为 Spring Bean。 */
    private LogFileExportUtil() {
    }

    /** 按服务器时区过滤记录，以 UTF-8 写入输出流；流的生命周期由调用接口管理。 */
    public static void export(File file, Long startTime, OutputStream output) throws IOException {
        ExportContent content;
        try (RandomAccessFile input = new RandomAccessFile(file, "r");
             ReversedLinesFileReader reader = ReversedLinesFileReader.builder()
                .setFile(file).setBufferSize(8192).setCharset(StandardCharsets.UTF_8).get()) {
            content = readLogs(preserveFirstEmptyLine(reader, input), startTime);
        }
        Writer writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
        if (content.stoppedAtStartTime) {
            // 提前停止时只补读文件前导内容，到首个合法头部即结束，不扫描较早日志。
            writeLeadingContent(file, writer);
        }
        content.write(writer);
        writer.flush();
    }

    /** Commons IO 2.16.1 在文件无末尾换行时会跳过第一个真实空行，这里按原始字节位置补回。 */
    private static ReverseLineReader preserveFirstEmptyLine(ReversedLinesFileReader reader, RandomAccessFile input)
            throws IOException {
        long length = input.length();
        if (length == 0 || isLineBreak(byteAt(input, length - 1))) {
            return reader::readLine;
        }
        return new ReverseLineReader() {
            private long position = length;

            /** 只跟踪首个空行之前的记录，补回后继续直接使用原读取器。 */
            @Override
            public String readLine() throws IOException {
                if (position < 0) {
                    return reader.readLine();
                }
                int lastByte = byteAt(input, position - 1);
                if (isLineBreak(lastByte)) {
                    position--;
                    if (lastByte == '\n' && position > 0 && byteAt(input, position - 1) == '\r') {
                        position--;
                    }
                }
                if (position == 0 || isLineBreak(byteAt(input, position - 1))) {
                    // 先返回真实空行，下次原读取器恰好跳过这一行，使两者位置重新一致。
                    position = -1;
                    return "";
                }
                String line = reader.readLine();
                if (line == null) {
                    position = -1;
                } else {
                    position -= line.getBytes(StandardCharsets.UTF_8).length;
                    if (position == 0) {
                        position = -1;
                    }
                }
                return line;
            }
        };
    }

    /** 按位置只读检查行分隔符，修复过程不会写入源日志或扫描尚未请求的记录。 */
    private static int byteAt(RandomAccessFile input, long position) throws IOException {
        input.seek(position);
        return input.read();
    }

    /** UTF-8 的 LF、CRLF 行分隔符均由 ASCII 字节组成。 */
    private static boolean isLineBreak(int value) {
        return value == '\n' || value == '\r';
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

    /** 同组较早消息共用此索引，候选位置全部来自最新的完整非空消息。 */
    private static final class StackTraceIndex {
        private final List<String> reference;
        private final int[] stackLengths;
        private final Map<List<String>, List<Integer>> candidates = new HashMap<>();

        /** 从后往前计算各位置的连续堆栈长度，只为完整的七行窗口建立索引。 */
        private StackTraceIndex(String message) {
            reference = message.lines().toList();
            stackLengths = new int[reference.size() + 1];
            for (int i = reference.size() - 1; i >= 0; i--) {
                if (STACK_TRACE_LINE.matcher(reference.get(i)).matches()) {
                    stackLengths[i] = stackLengths[i + 1] + 1;
                }
            }
            for (int i = 0; i < reference.size(); i++) {
                if (stackLengths[i] >= MIN_STACK_MATCH) {
                    List<String> window = reference.subList(i, i + MIN_STACK_MATCH);
                    candidates.computeIfAbsent(window, key -> new ArrayList<>()).add(i);
                }
            }
        }

        /** 同一窗口可能在基准中多次出现，逐行精确比较，选取该起点的最长匹配。 */
        private int longestMatch(List<String> lines, int start) {
            List<Integer> positions = candidates.get(lines.subList(start, start + MIN_STACK_MATCH));
            if (positions == null) {
                return 0;
            }
            int remaining = lines.size() - start;
            int longest = 0;
            for (int position : positions) {
                // 基准中的非堆栈行、真实空行均为边界，不同缩进和行号也会终止延伸。
                int limit = Math.min(remaining, stackLengths[position]);
                if (limit <= longest) {
                    continue;
                }
                int length = MIN_STACK_MATCH;
                while (length < limit && reference.get(position + length).equals(lines.get(start + length))) {
                    length++;
                }
                longest = Math.max(longest, length);
                if (longest == remaining) {
                    break;
                }
            }
            return longest;
        }

        /** 按较早消息的原文顺序折叠互不重叠的匹配段，未匹配部分逐行保留。 */
        private String fold(String message) {
            if (candidates.isEmpty()) {
                return message;
            }
            List<String> lines = message.lines().toList();
            int unwritten = 0;
            StringBuilder result = new StringBuilder();
            for (int start = 0; start + MIN_STACK_MATCH <= lines.size();) {
                int length = longestMatch(lines, start);
                if (length == 0) {
                    start++;
                    continue;
                }
                for (int i = unwritten; i <= start; i++) {
                    result.append(lines.get(i)).append('\n');
                }
                // 首尾两行保留原文，制表符后的说明仅计入中间实际省略的行数。
                result.append("\t已省略").append(length - 2).append("行重复堆栈\n");
                start += length;
                result.append(lines.get(start - 1)).append('\n');
                unwritten = start;
            }
            if (unwritten == 0) {
                return message;
            }
            for (int i = unwritten; i < lines.size(); i++) {
                result.append(lines.get(i)).append('\n');
            }
            return result.toString();
        }
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

    /** 重复入参和 SQL 在原位置输出行数说明，整段重复的消息继续省略。 */
    private static String repeatedPartNotice(String part, int partIndex) {
        if (partIndex == 2) {
            return "";
        }
        String name = partIndex == 0 ? "请求入参" : "执行SQL语句";
        // 正文已统一为 LF，lines 保留内部真实空行，忽略终止换行产生的虚拟空项。
        return "已省略相同" + name + part.lines().count() + "行\n";
    }

    /** 每部分有独立的去重集合，线程、租户、URL 均不影响同一调用位置的分组。 */
    private static class LogGroup {
        private final Deque<LogRecord> records = new ArrayDeque<>();
        private final List<Set<String>> seenParts = List.of(new HashSet<>(), new HashSet<>(), new HashSet<>());
        // 逆向首次遇到非空消息时建立一次索引，该消息完整输出且始终作为同组基准。
        private StackTraceIndex latestStackIndex;

        /** 完整正文挂载在最后一次出现的位置，更早的重复入参和 SQL 输出省略说明。 */
        private void add(String header, String body) {
            String[] parts = splitBody(body);
            for (int i = 0; i < parts.length; i++) {
                // 去重集合只存原文，说明仅替换输出内容，空部分无需生成说明。
                if (!parts[i].isEmpty() && !seenParts.get(i).add(parts[i])) {
                    parts[i] = repeatedPartNotice(parts[i], i);
                }
            }
            if (!parts[2].isEmpty()) {
                if (latestStackIndex == null) {
                    latestStackIndex = new StackTraceIndex(parts[2]);
                } else {
                    // 整段消息去重使用原文，只有仍需输出的较早消息才折叠重复片段。
                    parts[2] = latestStackIndex.fold(parts[2]);
                }
            }
            records.addFirst(new LogRecord(header, parts));
        }
    }

    /** 每次发生均保存原始头部，正文引用该位置应输出的原文或省略说明。 */
    private static class LogRecord {
        private final String header;
        private final String[] parts;

        /** 保存单条记录的输出信息，重复部分已替换为说明或空字符串。 */
        private LogRecord(String header, String[] parts) {
            this.header = header;
            this.parts = parts;
        }
    }
}
