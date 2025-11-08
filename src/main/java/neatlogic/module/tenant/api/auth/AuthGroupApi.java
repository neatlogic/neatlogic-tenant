/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.auth;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthBase;
import neatlogic.framework.auth.core.AuthFactory;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.module.ModuleGroupVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class AuthGroupApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "auth/group";
    }

    @Override
    public String getName() {
        return "获取权限组列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({
            @Param(name = "groupList", type = ApiParamType.JSONARRAY, desc = "权限组列表")
    })
    @Description(desc = "获取权限组列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        JSONArray groupArray = new JSONArray();
        groupArray.add(new JSONObject() {
            {
                this.put("text", "所有");
                this.put("value", "all");
            }
        });
        List<ModuleGroupVo> moduleGroupVos = TenantContext.get().getActiveModuleGroupList();
        if (CollectionUtils.isNotEmpty(moduleGroupVos)) {
            Map<String, List<AuthBase>> authGroupMap = AuthFactory.getAuthGroupMap();
            Set<String> groupSet = authGroupMap.keySet();
            for (ModuleGroupVo moduleGroupVo : moduleGroupVos) {
                String group = moduleGroupVo.getGroup();
                if (groupSet.contains(group)) {
                    JSONObject groupObj = new JSONObject();
                    groupObj.put("value", group);
                    groupObj.put("text", moduleGroupVo.getGroupName());
                    groupArray.add(groupObj);
                }
            }
        }
        returnObj.put("groupList", groupArray);
        return returnObj;
    }
}
