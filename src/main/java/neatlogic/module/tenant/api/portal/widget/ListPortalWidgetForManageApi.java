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
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dto.AuthorityVo;
import neatlogic.framework.dto.WorkAssignmentUnitVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.tenant.auth.portal.PORTAL_WIDGET_MANAGE;
import neatlogic.module.tenant.dao.mapper.portal.PortalMapper;
import neatlogic.module.tenant.dto.portal.PortalWidgetSearchVo;
import neatlogic.module.tenant.dto.portal.PortalWidgetVo;
import neatlogic.module.tenant.utils.PortalWidgetSearchUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = PORTAL_WIDGET_MANAGE.class)
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListPortalWidgetForManageApi extends PrivateApiComponentBase {
    @Resource
    private PortalMapper portalMapper;

    @Override
    public String getToken() {
        return "portal/widget/list/manage";
    }

    @Override
    public String getName() {
        return "nmtcapw.listportalwidgetformanageapi.getname";
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
    @Description(desc = "nmtcapw.listportalwidgetformanageapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        PortalWidgetSearchVo searchVo = paramObj.toJavaObject(PortalWidgetSearchVo.class);
        List<PortalWidgetVo> portalWidgetList = PortalWidgetSearchUtil.getPortalWidgetList(searchVo);
        for (PortalWidgetVo portalWidgetVo : portalWidgetList) {
            List<AuthorityVo> authorityVoList = portalMapper.getPortalWidgetAuthorityListByName(portalWidgetVo.getName());
            List<WorkAssignmentUnitVo> workAssignmentUnitList = new ArrayList<>();
            for (AuthorityVo authorityVo : authorityVoList) {
                WorkAssignmentUnitVo unitVo = new WorkAssignmentUnitVo();
                unitVo.setUuid(authorityVo.getUuid());
                unitVo.setInitType(authorityVo.getType());
                workAssignmentUnitList.add(unitVo);
            }
            portalWidgetVo.setAuthorityVoList(workAssignmentUnitList);
        }
        return TableResultUtil.getResult(PageUtil.subList(portalWidgetList, searchVo), searchVo);
    }
}
