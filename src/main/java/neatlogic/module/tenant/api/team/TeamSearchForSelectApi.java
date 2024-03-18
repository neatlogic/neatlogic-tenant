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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class TeamSearchForSelectApi extends PrivateApiComponentBase {

	@Autowired
	private TeamMapper teamMapper;

	@Override
	public String getToken() {
		return "team/search/forselect";
	}

	@Override
	public String getName() {
		return "查询分组_下拉";
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
			@Param(name = "list",
					type = ApiParamType.STRING,
					explode = ValueTextVo[].class,
					desc = "分组信息"),
			@Param(name = "pageCount",
					type = ApiParamType.INTEGER,
					desc = "总页数"),
			@Param(name = "currentPage",
					type = ApiParamType.INTEGER,
					desc = "当前页数"),
			@Param(name = "pageSize",
					type = ApiParamType.INTEGER,
					desc = "每页展示数量"),
			@Param(name = "rowNum",
					type = ApiParamType.INTEGER,
					desc = "总条目数")
	})
	@Description(desc = "查询分组_下拉")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		
		JSONObject resultObj = new JSONObject();
		TeamVo teamVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<TeamVo>() {});
		if (teamVo.getNeedPage()) {
			int rowNum = teamMapper.searchTeamCount(teamVo);
			resultObj.put("pageSize", teamVo.getPageSize());
			resultObj.put("currentPage", teamVo.getCurrentPage());
			resultObj.put("rowNum", rowNum);
			resultObj.put("pageCount", PageUtil.getPageCount(rowNum, teamVo.getPageSize()));
		}
		List<ValueTextVo> list = new ArrayList<>();
		teamVo.setIsDelete(0);
		List<TeamVo> teamList = teamMapper.searchTeam(teamVo);
		if (CollectionUtils.isNotEmpty(teamList)) {
			List<TeamVo> nameRepeatedCount = teamMapper.getRepeatTeamNameByNameList(teamList.stream().map(TeamVo::getName).collect(Collectors.toList()));
			Map<String, Integer> map = nameRepeatedCount.stream().collect(Collectors.toMap(e -> e.getName(), e -> e.getNameRepeatCount()));
			for(TeamVo team : teamList){
				ValueTextVo vo = new ValueTextVo();
				vo.setValue(GroupSearch.TEAM.getValuePlugin() + team.getUuid());
				/** 如果有重名的分组，找出其父分组的名称 **/
				if(map.get(team.getName()) > 1){
					TeamVo parent = teamMapper.getTeamByUuid(team.getParentUuid());
					if(parent != null){
						team.setParentName(parent.getName());
					}
				}
				vo.setText(StringUtils.isNotBlank(team.getParentName())
						? team.getName() + "(" + team.getParentName() + ")" : team.getName());
				list.add(vo);
			}
		}
		resultObj.put("list", list);
		return resultObj;
	}
}
