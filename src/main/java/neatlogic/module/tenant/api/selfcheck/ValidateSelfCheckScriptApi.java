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
import neatlogic.framework.auth.label.SELF_CHECK_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.selfcheck.SelfCheckService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = SELF_CHECK_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ValidateSelfCheckScriptApi extends PrivateApiComponentBase {
    @Resource
    private SelfCheckService selfCheckService;

    /** 返回 API 标识。 */
    @Override
    public String getToken() {
        return "selfcheck/script/validate";
    }

    /** 返回 API 名称。 */
    @Override
    public String getName() {
        return "校验功能自检脚本";
    }

    /** 仅编译脚本并返回语法诊断。 */
    @Input({@Param(name = "script", type = ApiParamType.STRING, isRequired = true, maxLength = 65536, desc = "JavaScript脚本")})
    @Description(desc = "仅编译校验功能自检脚本，不调用目标")
    @Override
    public Object myDoService(JSONObject paramObj) {
        return selfCheckService.validateScript(paramObj.getString("script"));
    }
}
