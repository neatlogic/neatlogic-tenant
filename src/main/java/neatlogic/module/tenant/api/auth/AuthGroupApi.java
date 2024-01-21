/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.auth;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthBase;
import neatlogic.framework.auth.core.AuthFactory;
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
