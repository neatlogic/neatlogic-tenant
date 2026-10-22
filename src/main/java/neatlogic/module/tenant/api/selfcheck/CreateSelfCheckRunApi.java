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
package neatlogic.module.tenant.api.selfcheck;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.SELF_CHECK_EXECUTE;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dto.selfcheck.SelfCheckRunVo;
import neatlogic.module.tenant.service.selfcheck.SelfCheckService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = SELF_CHECK_EXECUTE.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class CreateSelfCheckRunApi extends PrivateApiComponentBase {
    @Resource
    private SelfCheckService selfCheckService;

    /** 返回 API 标识。 */
    @Override
    public String getToken() {
        return "selfcheck/run/create";
    }

    /** 返回 API 名称。 */
    @Override
    public String getName() {
        return "创建功能自检执行";
    }

    /** 创建异步场景执行并返回执行标识。 */
    @Input({@Param(name = "scenarioId", type = ApiParamType.LONG, isRequired = true, desc = "场景ID")})
    @Description(desc = "异步创建功能自检场景执行")
    @Override
    public Object myDoService(JSONObject paramObj) {
        SelfCheckRunVo runVo = selfCheckService.createRun(paramObj.getLong("scenarioId"));
        JSONObject result = new JSONObject();
        result.put("runId", runVo.getId());
        result.put("status", runVo.getStatus());
        return result;
    }
}
