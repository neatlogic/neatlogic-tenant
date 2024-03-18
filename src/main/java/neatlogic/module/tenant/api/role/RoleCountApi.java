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
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class RoleCountApi extends PrivateApiComponentBase {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public String getToken() {
        return "role/count";
    }

    @Override
    public String getName() {
        return "角色统计接口";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Output({
            @Param(name = "roleCount", type = ApiParamType.INTEGER, desc = "角色数目")
    })
    @Description(desc = "角色统计接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        int count = roleMapper.searchRoleCount(new RoleVo());
        returnObj.put("roleCount", count);
        return returnObj;
    }
}
