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

package neatlogic.module.tenant.api.util;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.javascript.JavascriptUtil;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class JavascriptTestApi extends PrivateApiComponentBase {
    @Override
    public String getName() {
        return "测试JavaScript脚本执行";
    }

    @Input({
            @Param(name = "data", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "参数"),
            @Param(name = "script", type = ApiParamType.STRING, isRequired = true, desc = "脚本")
    })
    @Output({
            @Param(name = "result", type = ApiParamType.BOOLEAN)
    })
    @Description(desc = "")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject data = jsonObj.getJSONObject("data");
        String script = jsonObj.getString("script");
        JSONObject paramObj = new JSONObject();
        paramObj.put("data", data);
        Object returnValue = JavascriptUtil.runScript(paramObj, script);
        Boolean result = Boolean.parseBoolean(returnValue.toString());
        JSONObject resultObj = new JSONObject();
        resultObj.put("result", result);
        return resultObj;
    }

    @Override
    public String getToken() {
        return "util/javascript";
    }
}
