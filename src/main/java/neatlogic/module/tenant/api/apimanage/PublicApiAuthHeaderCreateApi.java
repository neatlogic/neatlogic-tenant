/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.apimanage;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.INTERFACE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.ApiAuthTypeNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.auth.core.ApiAuthFactory;
import neatlogic.framework.restful.auth.core.IApiAuth;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

@Component
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class PublicApiAuthHeaderCreateApi extends PrivateApiComponentBase {


    @Override
    public String getToken() {
        return "apimanage/authheader/create";
    }

    @Override
    public String getName() {
        return "根据认证信息生成自定义接口验证头部信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "authType", type = ApiParamType.STRING, isRequired = true, desc = "认证方式"),
            @Param(name = "authData", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "认证信息")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.JSONOBJECT, desc = "头部信息")
    })
    @Description(desc = "根据认证信息生成自定义接口验证头部信息接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        IApiAuth handler = ApiAuthFactory.getApiAuth(jsonObj.getString("authType"));
        if (handler == null) {
            throw new ApiAuthTypeNotFoundException(jsonObj.getString("authType"));
        }
        return handler.createHeader(jsonObj.getJSONObject("authData"));
    }

}
