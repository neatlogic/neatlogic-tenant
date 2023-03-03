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
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.exception.role.RoleNotFoundException;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class RoleGetApi extends PrivateApiComponentBase {

	@Autowired
	private RoleMapper roleMapper;

	@Override
	public String getToken() {
		return "role/get";
	}

	@Override
	public String getName() {
		return "角色详细信息查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "uuid",
					type = ApiParamType.STRING,
					desc = "角色uuid",
					isRequired = true) })
	@Output({ @Param(explode = RoleVo.class) })
	@Description(desc = "角色详细信息查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		RoleVo roleVo = roleMapper.getRoleByUuid(uuid);
		if(roleVo == null) {
			throw new RoleNotFoundException(uuid);
		}
		int userCount = roleMapper.searchRoleUserCountByRoleUuid(uuid);
		roleVo.setUserCount(userCount);
		return roleVo;
	}
}
