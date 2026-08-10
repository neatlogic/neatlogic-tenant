/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.api.portal;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dao.mapper.portal.PortalMapper;
import neatlogic.module.tenant.dto.portal.PortalVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetCurrentUserPortalApi extends PrivateApiComponentBase {

    @Resource
    private PortalMapper portalMapper;

    @Override
    public String getToken() {
        return "portal/currentuser/get";
    }

    @Override
    public String getName() {
        return "nmtcap.getcurrentuserportalapi.getname";
    }

    @Input({
            @Param(name = "moduleGroup", type = ApiParamType.STRING, isRequired = true, desc = "common.modulegroup")
    })
    @Output({
            @Param(explode = PortalVo.class)
    })
    @Description(desc = "nmtcap.getcurrentuserportalapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String moduleGroup = paramObj.getString("moduleGroup");
        String userUuid = UserContext.get().getUserUuid(true);
        Long enablePortalId = portalMapper.getUserEnablePortalId(moduleGroup, userUuid);
        if (enablePortalId != null) {
            PortalVo portal = portalMapper.getPortalById(enablePortalId);
            if (portal != null && Objects.equals(portal.getIsActive(), 1)) {
                return portal;
            }
        }
        return null;
    }
}
