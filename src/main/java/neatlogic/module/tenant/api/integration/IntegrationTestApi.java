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

package neatlogic.module.tenant.api.integration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.INTEGRATION_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.exception.integration.IntegrationHandlerNotFoundException;
import neatlogic.framework.integration.core.IIntegrationHandler;
import neatlogic.framework.integration.core.IntegrationHandlerFactory;
import neatlogic.framework.integration.dto.IntegrationResultVo;
import neatlogic.framework.integration.dto.IntegrationVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.framework.integration.handler.FrameworkRequestFrom;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = INTEGRATION_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class IntegrationTestApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "integration/test";
    }

    @Override
    public String getName() {
        return "nmtai.integrationtestapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "url", type = ApiParamType.REGEX, desc = "nmtai.integrationtestapi.address", isRequired = true, rule = "^((http|ftp|https)://)(([a-zA-Z0-9\\._-]+)|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?"),
            @Param(name = "handler", type = ApiParamType.STRING, desc = "nmtai.integrationtestapi.component", isRequired = true, xss = true),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "common.config", isRequired = true)
    })
    @Description(desc = "nmtai.integrationtestapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        IntegrationVo integrationVo = JSON.toJavaObject(jsonObj, IntegrationVo.class);
        IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
        if (handler == null) {
            throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
        }
        if (!isMethodSupported(handler, integrationVo.getMethod())) {
            throw new ApiRuntimeException("当前集成处理器不支持请求方式：" + integrationVo.getMethod());
        }
        IntegrationResultVo resultVo = handler.sendRequest(integrationVo, FrameworkRequestFrom.TEST);
        try {
            handler.validate(resultVo);
        } catch (ApiRuntimeException ex) {
            resultVo.appendError(ex.getMessage());
        }
        return resultVo;
    }

    private boolean isMethodSupported(IIntegrationHandler handler, String method) {
        if (handler == null || StringUtils.isBlank(method)) {
            return false;
        }
        String[] methodList = handler.getMethod();
        if (methodList == null) {
            return false;
        }
        for (String supportMethod : methodList) {
            if (method.equalsIgnoreCase(supportMethod)) {
                return true;
            }
        }
        return false;
    }
}
