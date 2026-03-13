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
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.auth.label.INTEGRATION_MODIFY;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.exception.integration.IntegrationHandlerNotFoundException;
import neatlogic.framework.exception.integration.IntegrationNotFoundException;
import neatlogic.framework.integration.core.IIntegrationHandler;
import neatlogic.framework.integration.core.IntegrationHandlerFactory;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
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

import javax.annotation.Resource;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class IntegrationRunApi extends PrivateApiComponentBase {

    private static final String EXECUTE_ACTION = "execute";

    @Resource
    private IntegrationMapper integrationMapper;

    @Override
    public String getToken() {
        return "integration/run/{uuid}";
    }

    @Override
    public boolean isRaw() {
        return true;
    }

    @Override
    public String getName() {
        return "nmtai.integrationrunapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "uuid", type = ApiParamType.STRING, desc = "集成配置uuid", isRequired = true)})
    @Description(desc = "nmtai.integrationrunapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(jsonObj.getString("uuid"));
        if (integrationVo == null) {
            throw new IntegrationNotFoundException(jsonObj.getString("uuid"));
        }
        UserContext userContext = UserContext.get();
        if (!AuthActionChecker.check(INTEGRATION_MODIFY.class) || (userContext != null && !Boolean.TRUE.equals(userContext.getIsSuperAdmin()))) {
            int matchCount = integrationMapper.checkUserHasIntegrationAuthority(
                    integrationVo.getUuid(),
                    EXECUTE_ACTION,
                    userContext.getUserUuid(),
                    userContext.getTeamUuidList(),
                    userContext.getRoleUuidList()
            );
            if (matchCount == 0) {
                throw new PermissionDeniedException();
            }
        }
        jsonObj.remove("uuid");
        integrationVo.setParamObj(jsonObj);
        IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
        if (handler == null) {
            throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
        }

        IntegrationResultVo resultVo = handler.sendRequest(integrationVo, FrameworkRequestFrom.API);
        String resultJson = resultVo.getTransformedResult();
        if (StringUtils.isBlank(resultJson)) {
            resultJson = resultVo.getRawResult();
        }
        return JSON.parse(resultJson);
    }
}
