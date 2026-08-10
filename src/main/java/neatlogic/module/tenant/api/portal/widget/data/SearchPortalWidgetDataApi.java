/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.api.portal.widget.data;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.portal.widget.core.IPortalWidget;
import neatlogic.framework.portal.widget.core.PortalWidgetFactory;
import neatlogic.framework.portal.widgetdata.core.IPortalWidgetDataHandler;
import neatlogic.framework.portal.widgetdata.core.PortalWidgetDataHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.exception.portal.PortalWidgetNotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchPortalWidgetDataApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "portal/widget/data/search";
    }

    @Override
    public String getName() {
        return "nmtcapwd.searchportalwidgetdataapi.getname";
    }

    @Input({
            @Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "common.handler"),
            @Param(name = "portalWidgetName", type = ApiParamType.STRING, isRequired = true, desc = "common.name"),
            @Param(name = "param", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "nmtcapwd.searchportalwidgetdataapi.input.param.desc.param")
    })
    @Output({

    })
    @Description(desc = "nmtcapwd.searchportalwidgetdataapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String handler = paramObj.getString("handler");
        String portalWidgetName = paramObj.getString("portalWidgetName");
        JSONObject param = paramObj.getJSONObject("param");
        IPortalWidget portalWidget = PortalWidgetFactory.getPortalWidget(portalWidgetName);
        if (portalWidget == null) {
            throw new PortalWidgetNotFoundException(portalWidgetName);
        }
        IPortalWidgetDataHandler portalWidgetDataHandler = PortalWidgetDataHandlerFactory.getHandler(handler);
        if (portalWidgetDataHandler != null) {
            if (CollectionUtils.isNotEmpty(portalWidget.getPortalWidgetDataHandlerClassList())
                    && portalWidget.getPortalWidgetDataHandlerClassList().contains(portalWidgetDataHandler.getClass())
            ) {
                param = param != null ? param : new JSONObject();
                return portalWidgetDataHandler.getData(param);
            }
        }
        return null;
    }
}
