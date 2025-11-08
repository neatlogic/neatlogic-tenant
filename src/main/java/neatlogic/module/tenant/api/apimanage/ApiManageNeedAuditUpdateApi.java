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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.INTERFACE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.ApiNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentFactory;
import neatlogic.framework.restful.dao.mapper.ApiMapper;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.framework.util.RegexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class ApiManageNeedAuditUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private ApiMapper ApiMapper;

    @Override
    public String getToken() {
        return "apimanage/needaudit/udpate";
    }

    @Override
    public String getName() {
        return "接口启用审计接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "token", type = ApiParamType.REGEX, rule = RegexUtils.API_TOKEN, isRequired = true, desc = "token")
    })
    @Description(desc = "接口启用审计接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String token = jsonObj.getString("token");
        ApiVo apiVo = ApiMapper.getApiByToken(token);
        if (apiVo != null) {
            ApiMapper.updateApiNeedAuditByToken(token);
            return 1 - apiVo.getNeedAudit();
        } else {
            ApiVo ramApiVo = PrivateApiComponentFactory.getApiByToken(apiVo.getToken());
            if (ramApiVo == null) {
                ramApiVo = PublicApiComponentFactory.getApiByToken(apiVo.getToken());
                if (ramApiVo == null) {
                    throw new ApiNotFoundException(token);
                }
            }
            ramApiVo.setNeedAudit(1);
            ApiMapper.replaceApi(ramApiVo);
            return 1;
        }
    }

}
