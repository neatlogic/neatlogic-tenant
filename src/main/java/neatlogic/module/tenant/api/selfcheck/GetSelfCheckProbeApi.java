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
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.selfcheck.annotation.SelfCheckApi;
import org.springframework.stereotype.Service;

@Service
@SelfCheckApi(defaultScript = "return context.response.reachable === true;")
@AuthAction(action = SELF_CHECK_EXECUTE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetSelfCheckProbeApi extends PrivateApiComponentBase {
    /** 返回 API 标识。 */
    @Override
    public String getToken() {
        return "selfcheck/probe/get";
    }

    /** 返回 API 名称。 */
    @Override
    public String getName() {
        return "功能自检基础探针";
    }

    /** 返回不读写业务数据的基础探针结果。 */
    @Description(desc = "返回无业务数据的基础探针结果，用于验证内部 API 自检链路")
    @Output({
            @Param(name = "reachable", type = ApiParamType.BOOLEAN, desc = "是否可调用"),
            @Param(name = "status", type = ApiParamType.STRING, desc = "探针状态"),
            @Param(name = "message", type = ApiParamType.STRING, desc = "检查结果"),
            @Param(name = "timestamp", type = ApiParamType.LONG, desc = "服务器时间戳")
    })
    @Override
    public Object myDoService(JSONObject paramObj) {
        JSONObject result = new JSONObject();
        result.put("reachable", true);
        result.put("status", "OK");
        result.put("message", "内部API调用正常");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}
