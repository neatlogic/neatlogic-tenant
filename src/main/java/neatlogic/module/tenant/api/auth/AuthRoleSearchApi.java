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

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-03-13 12:03
 **/
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class AuthRoleSearchApi extends PrivateApiComponentBase {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public String getToken() {
        return "auth/role/search";
    }

    @Override
    public String getName() {
        return "权限角色查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "auth", desc = "权限", type = ApiParamType.STRING, isRequired = true)
    })
    @Output({
            @Param(name = "roleList", desc = "角色集合", explode = RoleVo[].class, type = ApiParamType.JSONARRAY)
    })
    @Description(desc = "权限角色查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        String auth = jsonObj.getString("auth");
        List<RoleVo> roleList = roleMapper.getRoleListByAuthName(auth);
        returnObj.put("roleList", roleList);
        returnObj.put("roleCount", roleList.size());
        return returnObj;
    }
}
