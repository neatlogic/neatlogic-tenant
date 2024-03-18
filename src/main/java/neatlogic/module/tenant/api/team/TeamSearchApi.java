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

package neatlogic.module.tenant.api.team;

import java.util.List;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dto.TeamVo;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class TeamSearchApi extends PrivateApiComponentBase {

	@Autowired
	private TeamMapper teamMapper;

	@Override
	public String getToken() {
		return "team/search";
	}

	@Override
	public String getName() {
		return "分组查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "keyword",
					type = ApiParamType.STRING,
					desc = "关键字",
					xss = true),
			@Param(name = "uuid",
					type = ApiParamType.STRING,
					desc = "分组uuid",
					isRequired = false),
			@Param(name = "parentUuid",
					type = ApiParamType.STRING,
					desc = "父分组uuid",
					isRequired = false),
			@Param(name = "level",
					type = ApiParamType.STRING,
					desc = "级别",
					isRequired = false),
			@Param(name = "currentPage",
					type = ApiParamType.INTEGER,
					desc = "当前页",
					isRequired = false),
			@Param(name = "pageSize",
					type = ApiParamType.INTEGER,
					desc = "每页数据条目",
					isRequired = false),
			@Param(name = "needPage",
				type = ApiParamType.BOOLEAN,
				desc = "是否需要分页，默认true",
				isRequired = false) 
	})
	@Output({
			@Param(name = "teamList",
					type = ApiParamType.STRING,
					explode = TeamVo[].class,
					desc = "分组信息"),
			@Param(explode=BasePageVo.class)
	})
	@Description(desc = "分组查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		TeamVo teamVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<TeamVo>() {});
		teamVo.setIsDelete(0);
		JSONObject returnObj = new JSONObject();
		if (teamVo.getNeedPage()) {
			int rowNum = teamMapper.searchTeamCount(teamVo);
			returnObj.put("pageSize", teamVo.getPageSize());
			returnObj.put("currentPage", teamVo.getCurrentPage());
			returnObj.put("rowNum", rowNum);
			returnObj.put("pageCount", PageUtil.getPageCount(rowNum, teamVo.getPageSize()));
		}
		List<TeamVo> teamList = teamMapper.searchTeam(teamVo);
		returnObj.put("teamList", teamList);
		return returnObj;
	}

}
