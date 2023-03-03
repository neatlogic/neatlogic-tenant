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

package neatlogic.module.tenant.api.team;

import java.util.List;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.TeamUserTitle;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.exception.team.TeamNotFoundException;
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class TeamUserListApi extends PrivateApiComponentBase  {

	@Autowired
	private TeamMapper teamMapper;

	@Autowired
	private UserMapper userMapper;
	
	@Override
	public String getToken() {
		return "team/user/list";
	}

	@Override
	public String getName() {
		return "分组用户成员列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
        @Param(name = "teamUuid", type = ApiParamType.STRING, isRequired = true, desc = "分组uuid")
	})
	@Output({
		@Param(name = "teamUserList", explode = UserVo[].class, desc = "分组用户成员列表")
	})
	@Description( desc = "分组用户成员列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String teamUuid = jsonObj.getString("teamUuid");
		if(teamMapper.checkTeamIsExists(teamUuid) == 0) {
			throw new TeamNotFoundException(teamUuid);
		}
		JSONObject resultObj = new JSONObject();
		List<UserVo> teamUserList = userMapper.getUserListByTeamUuid(teamUuid);
		if(CollectionUtils.isNotEmpty(teamUserList)){
			teamUserList.stream().forEach(o -> {
				if(StringUtils.isNotBlank(o.getTitle())){
					o.setTitleText(TeamUserTitle.getText(o.getTitle()));
				}
			});
		}
		resultObj.put("teamUserList", teamUserList);
		return resultObj;
	}

}
