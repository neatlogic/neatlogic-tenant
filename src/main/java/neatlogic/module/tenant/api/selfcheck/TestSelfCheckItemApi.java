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
import neatlogic.module.tenant.service.selfcheck.SelfCheckService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = SELF_CHECK_EXECUTE.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class TestSelfCheckItemApi extends PrivateApiComponentBase {
    @Resource
    private SelfCheckService selfCheckService;

    /** 返回 API 标识。 */
    @Override
    public String getToken() {
        return "selfcheck/item/test";
    }

    /** 返回 API 名称。 */
    @Override
    public String getName() {
        return "调试功能自检检查项";
    }

    /** 按正式链路调试单个检查项并记录审计结果。 */
    @Input({
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, maxLength = 100, xss = true, desc = "检查项名称"),
            @Param(name = "apiToken", type = ApiParamType.STRING, isRequired = true, desc = "自检API标识"),
            @Param(name = "requestParam", type = ApiParamType.JSONOBJECT, desc = "请求参数"),
            @Param(name = "verifyScript", type = ApiParamType.STRING, isRequired = true, maxLength = 65536, desc = "JavaScript验真脚本"),
            @Param(name = "scriptTimeout", type = ApiParamType.INTEGER, desc = "脚本超时秒数"),
            @Param(name = "apiTimeout", type = ApiParamType.INTEGER, desc = "API超时秒数")
    })
    @Description(desc = "按正式执行链路调试单个功能自检检查项并记录审计结果")
    @Override
    public Object myDoService(JSONObject paramObj) {
        return selfCheckService.testItem(paramObj).toDetailJson();
    }
}
