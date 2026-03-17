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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.INTEGRATION_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.AuthorityVo;
import neatlogic.framework.exception.integration.IntegrationNotFoundException;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = INTEGRATION_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class AuthIntegrationApi extends PrivateApiComponentBase {

    private static final String EXECUTE_ACTION = "execute";

    @Resource
    private IntegrationMapper integrationMapper;

    @Override
    public String getToken() {
        return "integration/auth/save";
    }

    @Override
    public String getName() {
        return "nmtai.authintegrationapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "term.framework.integrationuuid"),
            @Param(name = "authorityList", type = ApiParamType.JSONARRAY, desc = "nmtai.authintegrationapi.input.param.desc.authoritylist")
    })
    @Description(desc = "nmtai.authintegrationapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String integrationUuid = jsonObj.getString("uuid");
        if (integrationMapper.checkIntegrationExists(integrationUuid) == 0) {
            throw new IntegrationNotFoundException(integrationUuid);
        }
        integrationMapper.deleteIntegrationAuthorityByIntegrationUuidAndAction(integrationUuid, EXECUTE_ACTION);
        List<String> authorityList = jsonObj.getJSONArray("authorityList") != null
                ? jsonObj.getJSONArray("authorityList").toJavaList(String.class)
                : null;
        if (CollectionUtils.isNotEmpty(authorityList)) {
            List<AuthorityVo> authorityVoList = AuthorityVo.getAuthorityVoList(authorityList, EXECUTE_ACTION);
            for (AuthorityVo authorityVo : authorityVoList) {
                integrationMapper.insertIntegrationAuthority(integrationUuid, authorityVo);
            }
        }
        return null;
    }
}
