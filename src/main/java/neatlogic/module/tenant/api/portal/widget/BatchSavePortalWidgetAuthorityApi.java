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

import com.alibaba.fastjson.JSONArray;
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
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@AuthAction(action = PORTAL_WIDGET_MANAGE.class)
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class BatchSavePortalWidgetAuthorityApi extends PrivateApiComponentBase {
    @Resource
    private PortalMapper portalMapper;

    @Override
    public String getToken() {
        return "portal/widget/authority/batchsave";
    }

    @Override
    public String getName() {
        return "nmtcapw.batchsaveportalwidgetauthorityapi.getname";
    }

    @Input({
            @Param(name = "nameList", type = ApiParamType.JSONARRAY, isRequired = true, minSize = 1, desc = "common.name"),
            @Param(name = "authorityList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "common.authoritylist")
    })
    @Description(desc = "nmtcapw.batchsaveportalwidgetauthorityapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONArray nameArray = paramObj.getJSONArray("nameList");
        JSONArray authorityArray = paramObj.getJSONArray("authorityList");
        if (CollectionUtils.isNotEmpty(nameArray)) {
            List<AuthorityVo> authorityVoList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(authorityArray)) {
                List<String> authorityList = authorityArray.toJavaList(String.class);
                authorityVoList = AuthorityVo.getAuthorityVoList(authorityList, null);
            }
            List<String> nameList = nameArray.toJavaList(String.class);
            for (String name : nameList) {
                if (PortalWidgetFactory.getPortalWidget(name) != null) {
                    portalMapper.deletePortalWidgetAuthorityByName(name);
                    if (CollectionUtils.isNotEmpty(authorityVoList)) {
                        for (AuthorityVo authorityVo : authorityVoList) {
                            portalMapper.insertPortalWidgetAuthority(name, authorityVo);
                        }
                    }
                }
            }
        }
        return null;
    }
}
