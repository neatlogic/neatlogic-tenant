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
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.role.RoleNotFoundException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class RoleUserListApi extends PrivateApiComponentBase  {

	@Autowired
	private RoleMapper roleMapper;

	@Autowired
    private UserMapper userMapper;

	@Override
	public String getToken() {
		return "role/user/list";
	}

	@Override
	public String getName() {
		return "获取角色用户成员列表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
        @Param(name = "roleUuid", type = ApiParamType.STRING, isRequired = true, desc = "角色uuid"),
        @Param(name = "keyword", type = ApiParamType.STRING, isRequired = false, desc = "关键字"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页展示数量 默认10"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页")
	})
	@Output({
		@Param(name = "userList", explode = UserVo[].class, desc = "角色用户成员列表")
	})
	@Description( desc = "获取角色用户成员列表")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String roleUuid = jsonObj.getString("roleUuid");
		UserVo userVo = JSONObject.toJavaObject(jsonObj, UserVo.class);
		if(roleMapper.checkRoleIsExists(roleUuid) == 0) {
			throw new RoleNotFoundException(roleUuid);
		}
		int rowNom = userMapper.searchUserCount(userVo);
		userVo.setRowNum(rowNom);
		return TableResultUtil.getResult(userMapper.getUserListByRoleUuid(userVo), userVo);
	}

}
