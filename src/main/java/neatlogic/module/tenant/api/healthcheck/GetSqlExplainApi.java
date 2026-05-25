/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.api.healthcheck;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dao.mapper.healthcheck.SqlStatusMapper;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.store.mysql.DatabaseVendor;
import neatlogic.framework.store.mysql.DatasourceManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class GetSqlExplainApi extends PrivateApiComponentBase {
    @Resource
    private SqlStatusMapper sqlStatusMapper;

    @Input({
            @Param(name = "sql", type = ApiParamType.STRING, desc = "common.sql"),
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "common.tbodylist"),
            @Param(explode = BasePageVo.class),
            @Param(name = "sql", type = ApiParamType.STRING, desc = "common.sql"),
    })
    @Description(desc = "nmtah.getsqlexplainapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        String sql = paramObj.getString("sql");
        resultObj.put("sql", sql);
        resultObj.put("currentPage", 1);
        resultObj.put("pageCount", 1);
        int rowNum = 0;
        int pageSize = 0;
        if (StringUtils.isNotBlank(sql)) {
            // SQL监控前端传入原始SQL，这里统一拼接EXPLAIN后查询执行计划
            sql = "EXPLAIN " + sql;
            List<Map<String, Object>> linkedHashMapList = sqlStatusMapper.selectListBySql(sql);
            resultObj.put("tbodyList", linkedHashMapList);
            rowNum = linkedHashMapList.size();
            pageSize = (rowNum / 20 + 1) * 20;
            if (Objects.equals(DatasourceManager.getDatabaseId(), DatabaseVendor.MYSQL.getDatabaseId())) {
                JSONArray theadList = new JSONArray()
                        .fluentAdd(new JSONObject().fluentPut("key", "id").fluentPut("title", "id"))
                        .fluentAdd(new JSONObject().fluentPut("key", "select_type").fluentPut("title", "select_type"))
                        .fluentAdd(new JSONObject().fluentPut("key", "table").fluentPut("title", "table"))
                        .fluentAdd(new JSONObject().fluentPut("key", "partitions").fluentPut("title", "partitions"))
                        .fluentAdd(new JSONObject().fluentPut("key", "type").fluentPut("title", "type"))
                        .fluentAdd(new JSONObject().fluentPut("key", "possible_keys").fluentPut("title", "possible_keys"))
                        .fluentAdd(new JSONObject().fluentPut("key", "key").fluentPut("title", "key"))
                        .fluentAdd(new JSONObject().fluentPut("key", "key_len").fluentPut("title", "key_len"))
                        .fluentAdd(new JSONObject().fluentPut("key", "ref").fluentPut("title", "ref"))
                        .fluentAdd(new JSONObject().fluentPut("key", "rows").fluentPut("title", "rows"))
                        .fluentAdd(new JSONObject().fluentPut("key", "filtered").fluentPut("title", "filtered"))
                        .fluentAdd(new JSONObject().fluentPut("key", "Extra").fluentPut("title", "Extra"))
                        ;
                resultObj.put("theadList", theadList);
            } else {
                List<String> theadKeyList = new ArrayList<>();
                JSONArray theadList = new JSONArray();
                for (Map<String, Object> map : linkedHashMapList) {
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        String key = entry.getKey();
                        if (!theadKeyList.contains(key)) {
                            theadKeyList.add(key);
                            theadList.add(new JSONObject().fluentPut("key", key).fluentPut("title", key));
                        }
                    }
                }
                resultObj.put("theadList", theadList);
            }
        }
        resultObj.put("pageSize", pageSize);
        resultObj.put("rowNum", rowNum);
        return resultObj;
    }

    @Override
    public String getToken() {
        return "/healthcheck/sqlexplain";
    }

    @Override
    public String getName() {
        return "nmtah.getsqlexplainapi.getname";
    }
}
