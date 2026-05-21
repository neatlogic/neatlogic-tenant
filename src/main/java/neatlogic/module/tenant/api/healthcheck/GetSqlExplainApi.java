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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.healthcheck.SqlStatusMapper;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class GetSqlExplainApi extends PrivateApiComponentBase {
    @Resource
    private SqlStatusMapper sqlStatusMapper;

    @Input({
            @Param(name = "sql", type = ApiParamType.STRING, desc = "sql语句"),
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "SQL执行计划表格数据"),
            @Param(name = "sql", type = ApiParamType.STRING, desc = "EXPLAIN SQL语句"),
    })
    @Description(desc = "获取SQL执行计划")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        String sql = paramObj.getString("sql");
        resultObj.put("sql", sql);
        if (StringUtils.isNotBlank(sql)) {
            // SQL监控前端传入原始SQL，这里统一拼接EXPLAIN后查询执行计划
            sql = "EXPLAIN " + sql;
            List<Map<String, Object>> linkedHashMapList = sqlStatusMapper.selectListBySql(sql);
            resultObj.put("tbodyList", linkedHashMapList);
        }
        return resultObj;
    }

    @Override
    public String getToken() {
        return "/healthcheck/sqlexplain";
    }

    @Override
    public String getName() {
        return "获取SQL执行计划";
    }
}
