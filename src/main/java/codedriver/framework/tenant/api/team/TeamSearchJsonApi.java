package codedriver.framework.tenant.api.team;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.AuthAction;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.service.TeamService;

@AuthAction(name="SYSTEM_TEAM_EDIT")
@Service
public class TeamSearchJsonApi extends ApiComponentBase{

	@Autowired
	private TeamService teamService;
	
	@Override
	public String getToken() {
		return "team/search/name";
	}

	@Override
	public String getName() {
		return "根据组名模糊查询组";
	}
	
	@Override
	public String getConfig() {
		return null;
	}


	@Input({ @Param(name = "name", type = ApiParamType.STRING, desc = "组名,模糊查询",isRequired=false),
		@Param(name = "limit", type = ApiParamType.STRING, desc = "返回数量",isRequired=false),
		@Param(name = "module", type = ApiParamType.STRING, desc = "模块名",isRequired=false),
		@Param(name = "componentId", type = ApiParamType.LONG, desc = "组件Id",isRequired=false)})
	@Output({@Param(name = "value", type = ApiParamType.STRING, desc = "组Id"),
		@Param(name = "text", type = ApiParamType.STRING, desc = "显示名")})
	@Description(desc = "根据条件查询组(组名模糊查询,根据模块查询,根据组件查询),返回{text,value}格式")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		
		TeamVo teamVo = new TeamVo();
		teamVo.setName(jsonObj.getString("name"));
		teamVo.setPageSize(jsonObj.getInteger("limit"));
		teamVo.setModule(jsonObj.getString("module"));
		teamVo.setComponentId(jsonObj.getInteger("componentId"));


		List<TeamVo> teamList = teamService.searchTeamByName(teamVo);
		JSONArray jsonList = new JSONArray();
		for (TeamVo team : teamList) {
			JSONObject json = new JSONObject();
			json.put("value", team.getUuid());
			String parentName = StringUtils.isNotBlank(team.getParentName()) ? "(" + team.getParentName() + ")" : "";
			json.put("text", team.getName() + parentName);
			jsonList.add(json);
		}
		return jsonList;
	}
}
