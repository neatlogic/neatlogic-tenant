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
