/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.api.util;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.plugin.CompressHandler;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.GzipUtil;
import neatlogic.module.tenant.dao.mapper.TestMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class ToggleTableGzipContentApi extends PrivateApiComponentBase {

    @Resource
    private TestMapper testMapper;

    @Override
    public String getName() {
        return "转换表压缩内容";
    }

    @Input({
            @Param(name = "tableName", type = ApiParamType.STRING, isRequired = true, desc = "表名"),
            @Param(name = "columnName", type = ApiParamType.STRING, isRequired = true, desc = "字段名"),
            @Param(name = "action", type = ApiParamType.ENUM, rule = "compress,uncompress", isRequired = true, desc = "操作，压缩或解压")
    })
    @Output({
            @Param(name = "updateCount", type = ApiParamType.INTEGER, isRequired = true, desc = "影响行数"),
    })
    @Description(desc = "转换表压缩内容")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String tableName = paramObj.getString("tableName");
        String columnName = paramObj.getString("columnName");
        String action = paramObj.getString("action");
        JSONObject resultObj = new JSONObject();
        // 先查询表结构，检查字段是否存在，压缩时字段是否是text、mediumtext、longtext类型
        Object fieldType = null;
        List<Map<String, Object>> tableStructureList = testMapper.getDatabaseTableStructure(tableName);
        for (Map<String, Object> map : tableStructureList) {
            Object field = map.get("Field");
            if (Objects.equals(field, columnName)) {
                fieldType = map.get("Type");
            }
        }
        if (fieldType == null) {
            resultObj.put("message", "数据库表`" + tableName + "`没有`" + columnName + "`字段");
            return resultObj;
        }
        CompressHandler compressHandler = null;
        if (Objects.equals(action, "compress")) {
            List<String> list = Arrays.asList("text", "mediumtext", "longtext");
            if (!list.contains(fieldType.toString())) {
                resultObj.put("message", "数据库表`" + tableName + "` 字段`" + columnName + "`是" +fieldType + "类型" + "不能压缩");
                return resultObj;
            }
            compressHandler = new CompressHandler();
        }
        int rowNum = testMapper.getGzipContentCountByTableNameAndColumnName(tableName, columnName, action);
        String prefix = "GZIP:";
        int updateCount = 0;
        while (updateCount < rowNum) {
            List<Map<String, String>> gzipContentList = testMapper.getGzipContentListByTableNameAndColumnName(tableName, columnName, action);
            if (CollectionUtils.isNotEmpty(gzipContentList)) {
                for (Map<String, String> map : gzipContentList) {
                    if (MapUtils.isNotEmpty(map)) {
                        String content = map.get("content");
                        if (StringUtils.isNotBlank(content)) {
                            if (Objects.equals(action, "uncompress")) {
                                if (content.startsWith(prefix)) {
                                    String newContent = GzipUtil.uncompress(content.substring(prefix.length()));
                                    testMapper.updateGzipContentByTableNameAndColumnName(tableName, columnName, content, newContent);
                                    updateCount++;
                                }
                            } else if (Objects.equals(action, "compress")) {
                                if (!content.startsWith(prefix) && compressHandler != null) {
                                    String newContent = compressHandler.handleParameter(content);
                                    testMapper.updateGzipContentByTableNameAndColumnName(tableName, columnName, content, newContent);
                                    updateCount++;
                                }
                            }
                        }
                    }
                }
            } else {
                break;
            }
        }
        resultObj.put("updateCount", updateCount);
        return resultObj;
    }

    @Override
    public String getToken() {
        return "/util/table/togglegzipcontent";
    }
}
