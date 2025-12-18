/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.log;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.constvalue.SystemProperty;
import neatlogic.framework.exception.SystemPropertyNotFoundException;
import neatlogic.framework.exception.file.FileNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.ServerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import static com.alibaba.fastjson.util.IOUtils.UTF8;

@AuthAction(action = ADMIN.class)
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetLogContentApi extends PrivateApiComponentBase {

    private final int DEFAULT_BUFFER_SIZE = 8192; // 8KB缓冲区
    // 读取最大行数
    private final int DEFAULT_MAX_LINES = 1000;
    // 读取最大字节数
    private final int DEFAULT_MAX_BYTES = 200 * DEFAULT_MAX_LINES;

    @Resource
    private ServerService serverService;

    @Override
    public String getName() {
        return "nmtal.getlogcontentapi.getname";
    }

    @Input({
            @Param(name = "serverId", type = ApiParamType.INTEGER, isRequired = true, desc = "term.framework.serverid"),
            @Param(name = "fileName", type = ApiParamType.STRING, isRequired = true, desc = "common.filename"),
            @Param(name = "fileSize", type = ApiParamType.LONG, desc = "common.filesize")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.STRING, desc = "common.tbodylist")
    })
    @Description(desc = "nmtal.getlogcontentapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        Integer serverId = paramObj.getInteger("serverId");
        if (Objects.equals(serverId, Config.SCHEDULE_SERVER_ID)) {
            String fileName = paramObj.getString("fileName");
            String log4jHome = System.getProperties().getProperty(SystemProperty.LOG4J_HOME);
            if (log4jHome != null) {
                File file = new File(log4jHome + File.separator + fileName);
                if (file.exists()) {
                    if (file.isFile()) {
                        long length = file.length();
                        Long fileSize = paramObj.getLong("fileSize");
                        if (!Objects.equals(fileSize, length)) {
                            StringBuilder stringBuilder = readLastLines(file, DEFAULT_MAX_LINES, DEFAULT_MAX_BYTES);
                            resultObj.put("content", stringBuilder.toString());
                            resultObj.put("isRefresh", 1);
                        } else {
                            resultObj.put("isRefresh", 0);
                        }
                        resultObj.put("fileSize", length);
                        resultObj.put("filePath", file.getAbsolutePath());
                        return resultObj;
                    } else {
                        throw new FileNotFoundException(FileNotFoundException.Type.DIRECTORY, log4jHome);
                    }
                } else {
                    throw new FileNotFoundException(FileNotFoundException.Type.NONEXISTENT, log4jHome);
                }
            } else {
                throw new SystemPropertyNotFoundException(SystemProperty.LOG4J_HOME);
            }
        } else {
            return serverService.postOtherServerApi(paramObj,serverId);
        }
    }

    @Override
    public String getToken() {
        return "log/content/get";
    }

    /**
     * 读取文件最后几行
     * @param file
     * @param maxLines 最大行数
     * @param maxChars 最大字符数
     * @return
     * @throws IOException
     */
    private StringBuilder readLastLines(File file, int maxLines, int maxChars) throws IOException {
        StringBuilder result = new StringBuilder();
        try (FileChannel channel = FileChannel.open(Paths.get(file.toURI()), StandardOpenOption.READ)) {
            long fileSize = channel.size();
            // 分配堆内存缓冲区（JVM堆内）
            ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
            CharBuffer charBuffer = CharBuffer.allocate(DEFAULT_BUFFER_SIZE);
            CharsetDecoder decoder = UTF8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
            long position = fileSize;
            // 已读取行数
            int linesFound  = 0;
            // 已读取字符数
            int charsRead = 0;
            while (position > 0 && linesFound  <= maxLines && charsRead <= maxChars) {
                // 计算本次读取的起始位置和大小
                int readSize = (int) Math.min(DEFAULT_BUFFER_SIZE, position);
                position -= readSize;
                if (position == 0 && readSize < DEFAULT_BUFFER_SIZE) {
                    buffer = ByteBuffer.allocate(readSize);
                }
                // 清空缓冲区（准备重新写入）
                buffer.clear();
                // 设置当前读写位置
                channel.position(position);
                // 读取数据到缓冲区
                channel.read(buffer);
                // 切换为读取模式
                buffer.flip();
                // 解码字节到字符
                decoder.reset();
                charBuffer.clear();
                decoder.decode(buffer, charBuffer, true);
                // 切换为读取模式
                charBuffer.flip();

                // 反向扫描换行符
                int endPos = charBuffer.length();
                for (int i = endPos - 1; i >= 0; i--) {
                    if (charBuffer.get(i) == '\n') {
                        linesFound++;
                        if (linesFound > maxLines || charsRead > maxChars) {
                            // 截取从换行符到结尾部分
                            appendReversed(charBuffer, i + 1, endPos, result);
                            endPos = i;
                            break;
                        }
                    }
                    charsRead++;
                }
                if (linesFound <= maxLines && charsRead <= maxChars) {
                    appendReversed(charBuffer, 0, endPos, result);
                }
            }
        }
        return result.reverse();
    }

    private void appendReversed(CharBuffer src, int start, int end, StringBuilder dest) {
        for (int i = end - 1; i >= start; i--) {
            dest.append(src.get(i));
        }
    }

}
