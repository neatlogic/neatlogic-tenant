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
import neatlogic.framework.portal.widget.core.IPortalWidget;
import neatlogic.framework.portal.widget.core.PortalWidgetFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.auth.portal.PORTAL_WIDGET_MANAGE;
import neatlogic.module.tenant.dao.mapper.portal.PortalMapper;
import neatlogic.module.tenant.dto.portal.PortalWidgetVo;
import neatlogic.module.tenant.exception.portal.PortalWidgetNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = PORTAL_WIDGET_MANAGE.class)
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetPortalWidgetApi extends PrivateApiComponentBase {
    @Resource
    private PortalMapper portalMapper;

    @Override
    public String getToken() {
        return "portal/widget/get";
    }

    @Override
    public String getName() {
        return "nmtcapw.getportalwidgetapi.getname";
    }

    @Input({@Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "common.name")})
    @Output({@Param(explode = PortalWidgetVo.class)})
    @Description(desc = "nmtcapw.getportalwidgetapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String name = paramObj.getString("name");
        IPortalWidget portalWidget = PortalWidgetFactory.getPortalWidget(name);
        if (portalWidget == null) {
            throw new PortalWidgetNotFoundException(name);
        }
        PortalWidgetVo portalWidgetVo = new PortalWidgetVo();
        portalWidgetVo.setName(portalWidget.getValue());
        portalWidgetVo.setLabel(portalWidget.getText());
        List<AuthorityVo> authorityVoList = portalMapper.getPortalWidgetAuthorityListByName(name);
        portalWidgetVo.setAuthorityList(AuthorityVo.getAuthorityList(authorityVoList));
        return portalWidgetVo;
    }
}
