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

package neatlogic.module.tenant.api.healthcheck;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.healthcheck.SqlAuditManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import java.util.Locale;

/** 管理当前服务进程的 SQL 审计保存模式，切换时保留已有监控记录。 */
@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class UpdateSqlAuditModeApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "/healthcheck/sqlaudit/mode/update";
    }

    @Override
    public String getName() {
        return "nmtah.updatesqlauditmodeapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    /** 切换两类监控记录共用的保存策略，并返回实际生效模式。 */
    @Input({@Param(name = "saveMode", type = ApiParamType.ENUM, isRequired = true, rule = "recent,slowest",
            desc = "nmtah.updatesqlauditmodeapi.savemode")})
    @Output({@Param(name = "saveMode", type = ApiParamType.ENUM, rule = "recent,slowest",
            desc = "nmtah.updatesqlauditmodeapi.savemode")})
    @Description(desc = "nmtah.updatesqlauditmodeapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) {
        JSONObject result = new JSONObject();
        // 框架枚举校验忽略大小写，写入前统一成缓存使用的标准值。
        result.put("saveMode", SqlAuditManager.updateSaveMode(paramObj.getString("saveMode").toLowerCase(Locale.ROOT)));
        return result;
    }
}
