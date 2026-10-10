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
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.healthcheck.SqlExplainService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class GetSqlExplainApi extends PrivateApiComponentBase {
    @Resource
    private SqlExplainService sqlExplainService;

    /** 保持传统表格响应，并按原始执行数据源获取结构化预估计划。 */
    @Input({
            @Param(name = "sql", type = ApiParamType.STRING, desc = "common.sql"),
            @Param(name = "includePlanJson", type = ApiParamType.INTEGER, rule = "0,1", defaultValue = "0", desc = "nmtah.getsqlexplainapi.includeplanjson"),
            @Param(name = "datasourceKey", type = ApiParamType.STRING, desc = "nmtah.getsqlexplainapi.datasourcekey"),
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "common.tbodylist"),
            @Param(explode = BasePageVo.class),
            @Param(name = "sql", type = ApiParamType.STRING, desc = "common.sql"),
            @Param(name = "planJson", type = ApiParamType.JSONOBJECT, desc = "nmtah.getsqlexplainapi.planjson"),
            @Param(name = "databaseName", type = ApiParamType.STRING, desc = "nmtah.getsqlexplainapi.databasename"),
    })
    @Description(desc = "nmtah.getsqlexplainapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        return sqlExplainService.getSqlExplain(paramObj.getString("sql"), paramObj.getString("datasourceKey"),
                paramObj.getInteger("includePlanJson"));
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
