/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.role;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.exception.role.RoleNotFoundException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-03-20 10:35
 **/
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class RoleGetListApi extends PrivateApiComponentBase {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public String getToken() {
        return "role/get/list";
    }

    @Override
    public String getName() {
        return "批量获取角色信息接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param( name = "roleUuidList", desc = "角色uuid集合", type = ApiParamType.JSONARRAY, isRequired = true)
    })
    @Output({
            @Param( name = "roleList", desc = "角色集合", explode = RoleVo[].class)
    })
    @Description(desc = "批量获取角色信息接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        List<String> roleUuidList = JSON.parseArray(jsonObj.getString("roleUuidList"), String.class);
        if (CollectionUtils.isNotEmpty(roleUuidList)){
            List<RoleVo> roleList = new ArrayList<>();
        	for (String roleUuid : roleUuidList){
        		RoleVo roleVo = roleMapper.getRoleByUuid(roleUuid);
        		if(roleVo == null) {
        			throw new RoleNotFoundException(roleUuid);
        		}
        		int userCount = roleMapper.searchRoleUserCountByRoleUuid(roleUuid);
        		roleVo.setUserCount(userCount);
                roleList.add(roleVo);
        	}
        	returnObj.put("roleList", roleList);
        }
        return returnObj;
    }
}
