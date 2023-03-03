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

package neatlogic.module.tenant.api.role;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dto.RoleAuthVo;
import neatlogic.framework.exception.role.RoleNotFoundException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class RoleAuthSearchApi extends PrivateApiComponentBase {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public String getToken() {
        return "role/auth/search";
    }

    @Override
    public String getName() {
        return "角色权限查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(
                    name = "roleUuidList",
                    type = ApiParamType.JSONARRAY,
                    desc = "角色uuid集合",
                    isRequired = true
            )
    })
    @Output({
            @Param(
                    name = "roleAuthList",
                    explode = RoleAuthVo[].class,
                    desc = "角色权限集合"
            )
    })
    @Description(desc = "角色权限查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        Map<String, Set<String>> roleAuthObj = new HashMap<>();
        List<String> roleUuidList = JSON.parseArray(jsonObj.getString("roleUuidList"), String.class);
        for (String roleUuid : roleUuidList){
        	if(roleMapper.checkRoleIsExists(roleUuid) == 0) {
        		throw new RoleNotFoundException(roleUuid);
        	}
            List<RoleAuthVo> roleAuthList = roleMapper.searchRoleAuthByRoleUuid(roleUuid);
            if (CollectionUtils.isNotEmpty(roleAuthList)){
                for (RoleAuthVo authVo : roleAuthList){
                	Set<String> authList = roleAuthObj.get(authVo.getAuthGroup());               	
                    if (authList == null){
                    	authList = new HashSet<>();
                    	roleAuthObj.put(authVo.getAuthGroup(), authList);
                    }
                    authList.add(authVo.getAuth());
                }
            }
        }
        returnObj.put("roleAuthList", roleAuthObj);
        return returnObj;
    }
}
