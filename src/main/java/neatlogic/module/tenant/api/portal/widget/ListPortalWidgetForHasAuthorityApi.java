/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.api.portal.widget;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.tenant.auth.portal.PORTAL_WIDGET_MANAGE;
import neatlogic.module.tenant.dao.mapper.portal.PortalMapper;
import neatlogic.module.tenant.dto.portal.PortalWidgetSearchVo;
import neatlogic.module.tenant.dto.portal.PortalWidgetVo;
import neatlogic.module.tenant.utils.PortalWidgetSearchUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListPortalWidgetForHasAuthorityApi extends PrivateApiComponentBase {
    @Resource
    private PortalMapper portalMapper;

    @Override
    public String getToken() {
        return "portal/widget/list/hasauthority";
    }

    @Override
    public String getName() {
        return "nmtcapw.listportalwidgetforhasauthorityapi.getname";
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "moduleGroup", type = ApiParamType.STRING, isRequired = true, desc = "common.modulegroup")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = PortalWidgetVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtcapw.listportalwidgetforhasauthorityapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        PortalWidgetSearchVo searchVo = paramObj.toJavaObject(PortalWidgetSearchVo.class);
        List<PortalWidgetVo> portalWidgetList = PortalWidgetSearchUtil.getPortalWidgetList(searchVo);
        if (CollectionUtils.isNotEmpty(portalWidgetList)) {
            Set<String> authorizedNameSet = new HashSet<>();
            Boolean hasAllAuthority = AuthActionChecker.check(ADMIN.class, PORTAL_WIDGET_MANAGE.class);
            if (!hasAllAuthority) {
                AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
                authorizedNameSet.addAll(portalMapper.getPortalWidgetNameListByAuthority(authenticationInfoVo));
            }
            for (int i = portalWidgetList.size() - 1; i >= 0; i--) {
                PortalWidgetVo portalWidgetVo = portalWidgetList.get(i);
                if (!hasAllAuthority && !authorizedNameSet.contains(portalWidgetVo.getName())) {
                    portalWidgetList.remove(i);
                }
            }
        }
        return TableResultUtil.getResult(PageUtil.subList(portalWidgetList, searchVo), searchVo);
    }
}
