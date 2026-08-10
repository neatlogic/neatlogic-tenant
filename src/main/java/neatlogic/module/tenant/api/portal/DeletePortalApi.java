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
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.auth.core.AuthFactory;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.$;
import neatlogic.module.tenant.auth.portal.PORTAL_MANAGE;
import neatlogic.module.tenant.dao.mapper.portal.PortalMapper;
import neatlogic.module.tenant.dto.portal.PortalVo;
import neatlogic.module.tenant.exception.portal.PortalNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class DeletePortalApi extends PrivateApiComponentBase {

    @Resource
    private PortalMapper portalMapper;

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "common.id")
    })
    @Output({})
    @Description(desc = "nmtcap.deleteportalapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        PortalVo portalVo = portalMapper.getPortalById(id);
        if (portalVo == null) {
            throw new PortalNotFoundException(id);
        }
        if (Objects.equals(portalVo.getType(), "global")) {
            if (!AuthActionChecker.check(ADMIN.class, PORTAL_MANAGE.class)) {
                throw new PermissionDeniedException(List.of(
                        $.t(AuthFactory.getAuthInstance(ADMIN.class.getSimpleName()).getAuthDisplayName()),
                        $.t(AuthFactory.getAuthInstance(PORTAL_MANAGE.class.getSimpleName()).getAuthDisplayName())
                ));
            }
        } else {
            if (!Objects.equals(portalVo.getFcu(), UserContext.get().getUserUuid())) {
                throw new PermissionDeniedException();
            }
        }
        portalMapper.deletePortalAuthorityByPortalId(id);
        portalMapper.deletePortalById(id);
        return null;
    }

    @Override
    public String getToken() {
        return "portal/delete";
    }

    @Override
    public String getName() {
        return "nmtcap.deleteportalapi.getname";
    }
}
