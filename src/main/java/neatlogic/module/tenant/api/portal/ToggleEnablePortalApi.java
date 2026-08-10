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
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dao.mapper.portal.PortalMapper;
import neatlogic.module.tenant.dto.portal.PortalVo;
import neatlogic.module.tenant.exception.portal.PortalIsNotActiveException;
import neatlogic.module.tenant.exception.portal.PortalNotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class ToggleEnablePortalApi extends PrivateApiComponentBase {

    @Resource
    private PortalMapper portalMapper;

    @Override
    public String getToken() {
        return "portal/toggleenable";
    }

    @Override
    public String getName() {
        return "nmtcap.toggleenableportalapi.getname";
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "id"),
            @Param(name = "action", type = ApiParamType.ENUM, rule = "save,delete", isRequired = true, desc = "common.action"),
    })
    @Description(desc = "nmtcap.toggleenableportalapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        PortalVo portalVo = portalMapper.getPortalById(id);
        if (portalVo == null) {
            throw new PortalNotFoundException(id);
        }
        if (Objects.equals(portalVo.getIsActive(), 0)) {
            throw new PortalIsNotActiveException(id);
        }
        if (Objects.equals(portalVo.getType(), "personal")) {
            if (!Objects.equals(portalVo.getFcu(), UserContext.get().getUserUuid())) {
                throw new PermissionDeniedException();
            }
        }
        String userUuid = UserContext.get().getUserUuid(true);
        String action = paramObj.getString("action");
        if (Objects.equals(action, "save")) {
            if (Objects.equals(portalVo.getType(), "personal")) {
                if (!Objects.equals(portalVo.getFcu(), userUuid)) {
                    throw new PermissionDeniedException();
                }
            } else {
                AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
                List<Long> portalIdList = portalMapper.getPortalIdListByAuthority(authenticationInfoVo);
                if (CollectionUtils.isEmpty(portalIdList) || !portalIdList.contains(id)) {
                    throw new PermissionDeniedException();
                }
            }
            portalMapper.insertUserEnablePortal(id, portalVo.getModuleGroup(), userUuid);
        } else if (Objects.equals(action, "delete")) {
            portalMapper.deleteUserEnablePortal(portalVo.getModuleGroup(), userUuid);
        }
        return null;
    }
}
