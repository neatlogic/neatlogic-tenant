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
import neatlogic.framework.common.util.ModuleUtil;
import neatlogic.framework.dto.AuthorityVo;
import neatlogic.framework.dto.module.ModuleGroupVo;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.$;
import neatlogic.framework.util.SnowflakeUtil;
import neatlogic.module.tenant.auth.portal.PORTAL_MANAGE;
import neatlogic.module.tenant.dao.mapper.portal.PortalMapper;
import neatlogic.module.tenant.dto.portal.PortalVo;
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
@OperationType(type = OperationTypeEnum.CREATE)
public class SavePortalApi extends PrivateApiComponentBase {

    @Resource
    private PortalMapper portalMapper;

    @Override
    public String getToken() {
        return "portal/save";
    }

    @Override
    public String getName() {
        return "nmtcap.saveportalapi.getname";
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "common.id"),
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, maxLength = 200, desc = "common.name"),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "common.isactive"),
            @Param(name = "moduleGroup", type = ApiParamType.STRING, isRequired = true, desc = "common.modulegroup"),
            @Param(name = "type", type = ApiParamType.ENUM, rule = "global,personal", isRequired = true, desc = "common.type"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "common.config"),
            @Param(name = "authorityList", type = ApiParamType.JSONARRAY, desc = "common.authoritylist")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.LONG, desc = "common.id")
    })
    @Description(desc = "nmtcap.saveportalapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String userUuid = UserContext.get().getUserUuid();
        PortalVo portalVo = paramObj.toJavaObject(PortalVo.class);
        if (portalVo.getId() != null) {
            PortalVo oldPortalVo = portalMapper.getPortalById(portalVo.getId());
            if (oldPortalVo == null) {
                throw new PortalNotFoundException(portalVo.getId());
            }
            if (Objects.equals(oldPortalVo.getType(), "global")) {
                if (!AuthActionChecker.check(ADMIN.class, PORTAL_MANAGE.class)) {
                    throw new PermissionDeniedException(List.of(
                            $.t(AuthFactory.getAuthInstance(ADMIN.class.getSimpleName()).getAuthDisplayName()),
                            $.t(AuthFactory.getAuthInstance(PORTAL_MANAGE.class.getSimpleName()).getAuthDisplayName())
                    ));
                }
            } else {
                if (!Objects.equals(oldPortalVo.getFcu(), userUuid)) {
                    throw new PermissionDeniedException();
                }
            }
            portalVo.setId(portalVo.getId());
            portalVo.setSort(oldPortalVo.getSort());
            portalVo.setModuleGroup(oldPortalVo.getModuleGroup());
            portalVo.setType(oldPortalVo.getType());
            portalMapper.deletePortalAuthorityByPortalId(portalVo.getId());
        } else {
            if (Objects.equals(portalVo.getType(), "global")) {
                if (!AuthActionChecker.check(ADMIN.class, PORTAL_MANAGE.class)) {
                    throw new PermissionDeniedException(List.of(
                            $.t(AuthFactory.getAuthInstance(ADMIN.class.getSimpleName()).getAuthDisplayName()),
                            $.t(AuthFactory.getAuthInstance(PORTAL_MANAGE.class.getSimpleName()).getAuthDisplayName())
                    ));
                }
            }
            if (!Objects.equals(portalVo.getModuleGroup(), "index")) {
                ModuleGroupVo moduleGroupVo = ModuleUtil.getModuleGroup(portalVo.getModuleGroup());
                if (moduleGroupVo == null) {
                    throw new ParamIrregularException("moduleGroup", $.t("nfem.modulegroupnotfoundexception.modulegroupnotfoundexception", portalVo.getModuleGroup()));
                }
            }
            portalVo.setId(SnowflakeUtil.uniqueLong());
            Integer maxSort = portalMapper.getMaxSort();
            portalVo.setSort(maxSort == null ? 1 : maxSort + 1);
            portalVo.setFcu(userUuid);
        }
        if (Objects.equals(portalVo.getType(), "personal")) {
            portalVo.setIsActive(1);
        }
        portalVo.setLcu(userUuid);
        portalMapper.insertPortal(portalVo);
        List<String> authorityList = portalVo.getAuthorityList();
        if (CollectionUtils.isNotEmpty(authorityList)) {
            for (AuthorityVo authorityVo : AuthorityVo.getAuthorityVoList(authorityList, null)) {
                portalMapper.insertPortalAuthority(portalVo.getId(), authorityVo);
            }
        }
        return portalVo.getId();
    }
}
