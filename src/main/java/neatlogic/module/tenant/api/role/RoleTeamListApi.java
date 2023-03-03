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

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dto.RoleTeamVo;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.role.RoleNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class RoleTeamListApi extends PrivateApiComponentBase  {

	@Autowired
	private RoleMapper roleMapper;

	@Autowired
    private TeamMapper teaMapper;

	@Override
	public String getToken() {
		return "role/team/list";
	}

	@Override
	public String getName() {
		return "查询角色分组列表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
        @Param(name = "roleUuid", type = ApiParamType.STRING, isRequired = true, desc = "角色uuid")
	})
	@Output({
		@Param(name = "tbodyList", explode = UserVo[].class, desc = "角色用户成员列表")
	})
	@Description( desc = "查询角色分组列表")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String roleUuid = jsonObj.getString("roleUuid");
		if(roleMapper.checkRoleIsExists(roleUuid) == 0) {
			throw new RoleNotFoundException(roleUuid);
		}
		JSONObject resultObj = new JSONObject();
		List<RoleTeamVo> roleTeamList = roleMapper.getRoleTeamListByRoleUuid(roleUuid);
		if (CollectionUtils.isNotEmpty(roleTeamList)) {
			List<String> teamUuidList = new ArrayList<>();
			Map<String, Integer> checkedChildrenMap = new HashMap<>();
			for (RoleTeamVo roleTeamVo : roleTeamList) {
				teamUuidList.add(roleTeamVo.getTeamUuid());
				checkedChildrenMap.put(roleTeamVo.getTeamUuid(), roleTeamVo.getCheckedChildren());
			}
			List<TeamVo> teamList = teaMapper.getTeamByUuidList(teamUuidList);
			for (TeamVo teamVo : teamList) {
				Integer checkedChildren = checkedChildrenMap.get(teamVo.getUuid());
				if (checkedChildren != null) {
					teamVo.setCheckedChildren(checkedChildren);
				}
			}
			resultObj.put("tbodyList", teamList);
		} else {
			resultObj.put("tbodyList", new ArrayList<>());
		}
		return resultObj;
	}

}
