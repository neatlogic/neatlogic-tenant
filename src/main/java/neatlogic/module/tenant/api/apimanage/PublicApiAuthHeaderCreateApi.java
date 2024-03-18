/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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
