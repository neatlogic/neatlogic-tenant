/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
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
