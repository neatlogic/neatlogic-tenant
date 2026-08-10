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
import neatlogic.framework.dto.AuthorityVo;
import neatlogic.framework.portal.widget.core.PortalWidgetFactory;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.auth.portal.PORTAL_WIDGET_MANAGE;
import neatlogic.module.tenant.dao.mapper.portal.PortalMapper;
import neatlogic.module.tenant.dto.portal.PortalWidgetVo;
import neatlogic.module.tenant.exception.portal.PortalWidgetNotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@AuthAction(action = PORTAL_WIDGET_MANAGE.class)
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class SavePortalWidgetApi extends PrivateApiComponentBase {
    @Resource
    private PortalMapper portalMapper;

    @Override
    public String getToken() {
        return "portal/widget/save";
    }

    @Override
    public String getName() {
        return "nmtcapw.saveportalwidgetapi.getname";
    }

    @Input({
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "common.name"),
            @Param(name = "authorityList", type = ApiParamType.JSONARRAY, desc = "common.authoritylist")
    })
    @Description(desc = "nmtcapw.saveportalwidgetapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("name", paramObj.getString("name"));
        jsonObj.put("authorityList", paramObj.getJSONArray("authorityList"));
        PortalWidgetVo portalWidgetVo = jsonObj.toJavaObject(PortalWidgetVo.class);
        if (PortalWidgetFactory.getPortalWidget(portalWidgetVo.getName()) == null) {
            throw new PortalWidgetNotFoundException(portalWidgetVo.getName());
        }
        portalMapper.deletePortalWidgetAuthorityByName(portalWidgetVo.getName());
        if (CollectionUtils.isNotEmpty(portalWidgetVo.getAuthorityList())) {
            for (AuthorityVo authorityVo : AuthorityVo.getAuthorityVoList(portalWidgetVo.getAuthorityList(), null)) {
                portalMapper.insertPortalWidgetAuthority(portalWidgetVo.getName(), authorityVo);
            }
        }
        return null;
    }
}
