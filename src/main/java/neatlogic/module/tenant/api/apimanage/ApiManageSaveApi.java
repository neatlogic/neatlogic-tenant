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

package neatlogic.module.tenant.api.apimanage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.INTERFACE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.ApiNotFoundException;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.dao.mapper.ApiMapper;
import neatlogic.framework.restful.dto.ApiHandlerVo;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.framework.restful.enums.ApiType;
import neatlogic.framework.util.RegexUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

@Service
@Transactional
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class ApiManageSaveApi extends PrivateApiComponentBase {

    @Resource
    private ApiMapper ApiMapper;

    @Override
    public String getToken() {
        return "apimanage/save";
    }

    @Override
    public String getName() {
        return "nmtaa.apimanagesaveapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "token", type = ApiParamType.REGEX, rule = RegexUtils.API_TOKEN, isRequired = true, desc = "token"),
            @Param(name = "name", type = ApiParamType.STRING, maxLength = 50, isRequired = true, desc = "common.name"),
            @Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "nmtaa.apimanagesaveapi.input.param.desc.handler"),
            @Param(name = "needAudit", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "nmtaa.apimanagesaveapi.input.param.desc.needaudit"),
            @Param(name = "isMcp", type = ApiParamType.ENUM, rule = "0,1", desc = "是否MCP服务"),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "common.isactive"),
            @Param(name = "timeout", type = ApiParamType.INTEGER, desc = "nmtaa.apimanagesaveapi.input.param.desc.timeout"),
            @Param(name = "qps", type = ApiParamType.INTEGER, desc = "nmtaa.apimanagesaveapi.input.param.desc.qps"),
            @Param(name = "expire", type = ApiParamType.LONG, desc = "nmtaa.apimanagesaveapi.input.param.desc.expire"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "nmtaa.apimanagesaveapi.input.param.desc.config"),
            @Param(name = "username", type = ApiParamType.STRING, desc = "nmtaa.apimanagesaveapi.input.param.desc.username"),
            @Param(name = "password", type = ApiParamType.REGEX, rule = RegexUtils.PASSWORD, desc = "common.password"),
            @Param(name = "description", type = ApiParamType.STRING, desc = "common.description"),
    })
    @Description(desc = "nmtaa.apimanagesaveapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ApiVo apiVo = JSON.toJavaObject(jsonObj,ApiVo.class);
        ApiHandlerVo apiHandlerVo = PrivateApiComponentFactory.getApiHandlerByHandler(apiVo.getHandler());
        ApiVo ramApiVo = PrivateApiComponentFactory.getApiByToken(apiVo.getToken());
        if (apiHandlerVo == null) {
            throw new ParamIrregularException("handler");
        }
        apiVo.setType(apiHandlerVo.getType());
        apiVo.setModuleId(apiHandlerVo.getModuleId());
        if (Objects.equals(apiVo.getIsMcp(), 1) && !ApiType.OBJECT.getValue().equals(apiHandlerVo.getType())) {
            throw new ParamIrregularException("isMcp", "only object api can enable mcp");
        }
        ApiVo dbApiVo = ApiMapper.getApiByToken(apiVo.getToken());
        if (StringUtils.isNotBlank(apiVo.getUsername()) && StringUtils.isBlank(apiVo.getPassword()) && dbApiVo != null) {
            apiVo.setPasswordCipher(dbApiVo.getPasswordCipher());
        }
        if (ramApiVo == null) {
            throw new ApiNotFoundException(apiVo.getToken());
        }
        apiVo.setApiType(ramApiVo.getApiType());

        ApiMapper.replaceApi(apiVo);

        return null;
    }
}
