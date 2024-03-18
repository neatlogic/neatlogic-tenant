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

package neatlogic.module.tenant.api.integration;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.exception.integration.HttpMethodNotFoundException;
import neatlogic.framework.exception.integration.IntegrationHandlerNotFoundException;
import neatlogic.framework.exception.integration.IntegrationNameRepeatsException;
import neatlogic.framework.integration.authentication.enums.HttpMethod;
import neatlogic.framework.integration.core.IntegrationHandlerFactory;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.integration.dto.IntegrationVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.auth.label.INTERFACE_MODIFY;
import neatlogic.framework.util.RegexUtils;
import neatlogic.module.tenant.exception.integration.IntegrationUrlIllegalException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class IntegrationSaveApi extends PrivateApiComponentBase {

    @Resource
    private IntegrationMapper integrationMapper;

    @Override
    public String getToken() {
        return "integration/save";
    }

    @Override
    public String getName() {
        return "集成配置保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "uuid，为空代表新增"),
            @Param(name = "name", type = ApiParamType.STRING, desc = "名称", isRequired = true, xss = true),
            @Param(name = "url", type = ApiParamType.REGEX, desc = "目标地址", isRequired = true, rule = RegexUtils.CONNECT_URL),
            @Param(name = "handler", type = ApiParamType.STRING, desc = "组件", isRequired = true, xss = true),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "配置，json格式", isRequired = true)
    })
    @Description(desc = "集成配置保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        IntegrationVo integrationVo = JSONObject.toJavaObject(jsonObj, IntegrationVo.class);
        if (integrationMapper.checkNameIsRepeats(integrationVo) > 0) {
            throw new IntegrationNameRepeatsException(integrationVo.getName());
        }
        if (IntegrationHandlerFactory.getHandler(integrationVo.getHandler()) == null) {
            throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
        }
        if (HttpMethod.getHttpMethod(integrationVo.getMethod()) == null) {
            throw new HttpMethodNotFoundException(integrationVo.getMethod());
        }
        if (integrationVo.getUrl().contains("integration/run/")) {
            throw new IntegrationUrlIllegalException(integrationVo.getUrl());
        }
        integrationVo.setIsActive(1);
        if (StringUtils.isNotBlank(jsonObj.getString("uuid"))) {
            integrationMapper.updateIntegration(integrationVo);
        } else {
            integrationMapper.insertIntegration(integrationVo);
        }
        return null;
    }

    public IValid name() {
        return value -> {
            IntegrationVo integrationVo = JSONObject.toJavaObject(value, IntegrationVo.class);
            if (integrationMapper.checkNameIsRepeats(integrationVo) > 0) {
                return new FieldValidResultVo(new IntegrationNameRepeatsException(integrationVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }
}
