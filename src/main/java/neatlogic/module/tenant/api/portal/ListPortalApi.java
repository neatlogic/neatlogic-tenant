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
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.dto.AuthorityVo;
import neatlogic.framework.dto.WorkAssignmentUnitVo;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.$;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.tenant.auth.portal.PORTAL_MANAGE;
import neatlogic.module.tenant.dao.mapper.portal.PortalMapper;
import neatlogic.module.tenant.dto.portal.PortalSearchVo;
import neatlogic.module.tenant.dto.portal.PortalVo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
//@AuthAction(action = PORTAL_MANAGE.class)
//@AuthAction(action = ADMIN.class)
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListPortalApi extends PrivateApiComponentBase {

    @Resource
    private PortalMapper portalMapper;

    @Override
    public String getToken() {
        return "portal/list";
    }

    @Override
    public String getName() {
        return "nmtcap.listportalapi.getname";
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "common.isactive"),
            @Param(name = "type", type = ApiParamType.ENUM, rule = "global,personal", isRequired = true, desc = "common.type"),
            @Param(name = "moduleGroup", type = ApiParamType.STRING, isRequired = true, desc = "common.modulegroup"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage")
    })
    @Output({@Param(explode = BasePageVo.class), @Param(name = "tbodyList", explode = PortalVo[].class, desc = "common.tbodylist")})
    @Description(desc = "nmtcap.listportalapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        PortalSearchVo searchVo = paramObj.toJavaObject(PortalSearchVo.class);
        Long enablePortalId = null;
        if (Objects.equals(searchVo.getType(), "global")) {
            if (!AuthActionChecker.check(ADMIN.class, PORTAL_MANAGE.class)) {
                throw new PermissionDeniedException(List.of(
                        $.t(AuthFactory.getAuthInstance(ADMIN.class.getSimpleName()).getAuthDisplayName()),
                        $.t(AuthFactory.getAuthInstance(PORTAL_MANAGE.class.getSimpleName()).getAuthDisplayName())
                ));
            }
        } else {
            searchVo.setFcu(UserContext.get().getUserUuid());
            searchVo.setIsActive(1);
            AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
            List<Long> portalIdList = portalMapper.getPortalIdListByAuthority(authenticationInfoVo);
            if (CollectionUtils.isNotEmpty(portalIdList)) {
                searchVo.setGlobalPortalIdList(portalIdList);
            }
            enablePortalId = portalMapper.getUserEnablePortalId(searchVo.getModuleGroup(), UserContext.get().getUserUuid(true));
        }
        int rowNum = portalMapper.getPortalCount(searchVo);
        if (rowNum == 0) {
            return TableResultUtil.getResult(new ArrayList<>(), searchVo);
        }
        searchVo.setRowNum(rowNum);
        List<PortalVo> portalList = portalMapper.searchPortal(searchVo);
        for (PortalVo portalVo : portalList) {
            if (Objects.equals(portalVo.getId(), enablePortalId) && Objects.equals(portalVo.getIsActive(), 1)) {
                portalVo.setIsEnable(1);
            }
            if (Objects.equals(searchVo.getType(), "global") && Objects.equals(portalVo.getType(), "global")) {
                List<AuthorityVo> authorityVoList = portalMapper.getPortalAuthorityListByPortalId(portalVo.getId());
                List<WorkAssignmentUnitVo> workAssignmentUnitList = new ArrayList<>();
                for (AuthorityVo authorityVo : authorityVoList) {
                    WorkAssignmentUnitVo unitVo = new WorkAssignmentUnitVo();
                    unitVo.setUuid(authorityVo.getUuid());
                    unitVo.setInitType(authorityVo.getType());
                    workAssignmentUnitList.add(unitVo);
                }
                portalVo.setAuthorityVoList(workAssignmentUnitList);
            }
        }
        return TableResultUtil.getResult(portalList, searchVo);
    }
}
