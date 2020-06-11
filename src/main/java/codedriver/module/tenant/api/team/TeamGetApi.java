package codedriver.module.tenant.api.team;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class TeamGetApi extends ApiComponentBase {

	@Autowired
	private TeamMapper teamMapper;

	@Override
	public String getToken() {
		return "team/get";
	}

	@Override
	public String getName() {
		return "获取组信息接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "uuid",
					type = ApiParamType.STRING,
					desc = "分组uuid",
					minLength = 32,
					maxLength = 32,
					isRequired = true),
			@Param( name = "name",
					type = ApiParamType.STRING,
					desc = "分组名称"),
			@Param( name = "isEdit",
					type = ApiParamType.INTEGER,
					desc = "是否edit,0 为添加下级分组，1为编辑,",
					isRequired = true)
		})
	@Output({
			@Param(name = "teamVo",
					explode = TeamVo.class,
					desc = "组id") })
	@Description(desc = "获取组信息接口")
	@Override
	public Object myDoService(JSONObject jsonObj) {
		TeamVo team = new TeamVo();
		team.setName(jsonObj.getString("name"));
		team.setUuid(jsonObj.getString("uuid"));
		TeamVo teamVo = teamMapper.getTeam(team);
		int userCount = teamMapper.searchUserCountByTeamUuid(team.getUuid());
		teamVo.setUserCount(userCount);
		TeamVo parentTeam = null;
		List<String> pathNameList = new ArrayList<>();
		int isEdit = jsonObj.getIntValue("isEdit");
		if(isEdit == 1) {
			parentTeam = teamMapper.getTeamByUuid(teamVo.getParentUuid());
		}else {
			parentTeam = teamVo;
		}
		if (!parentTeam.getUuid().equals("0")){
			getTeamPath(parentTeam, pathNameList);
		}
		teamVo.setPathNameList(pathNameList);		
		return teamVo;
	}
	
	private void getTeamPath(TeamVo teamVo, List<String> pathNameList){
		if (!teamVo.getParentUuid().equals("0")){
			TeamVo parentTeam = teamMapper.getTeamByUuid(teamVo.getParentUuid());
			getTeamPath(parentTeam, pathNameList);
		}
		pathNameList.add(teamVo.getName());
	}
}
