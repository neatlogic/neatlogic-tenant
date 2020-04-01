package codedriver.module.tenant.api.team;

import codedriver.module.tenant.service.TeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
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
	private TeamService teamService;

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
					desc = "分组uuid"),
			@Param( name = "name",
					type = ApiParamType.STRING,
					desc = "分组名称")})
	@Output({
			@Param(name = "teamVo",
					explode = TeamVo.class,
					desc = "组id") })
	@Description(desc = "获取组信息接口")
	@Override
	public Object myDoService(JSONObject jsonObj) {
		TeamVo teamVo = new TeamVo();
		teamVo.setName(jsonObj.getString("name"));
		teamVo.setUuid(jsonObj.getString("uuid"));
		teamVo = teamService.getTeam(teamVo);
		return teamVo;
	}
}
