package codedriver.framework.tenant.api.team;

import java.util.List;

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
public class TeamGetTreePathApi extends ApiComponentBase{

	@Autowired
	private TeamService teamService;
	
	@Override
	public String getToken() {
		return "team/tree/path/get";
	}

	@Override
	public String getName() {
		return "根据组uuid查组完整树路径";
	}
	
	@Override
	public String getConfig() {
		return null;
	}


	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "组uuid",isRequired=false)})
	@Output({@Param(name = "uuid", type = ApiParamType.STRING, desc = "组uuid")})
	@Description(desc = "根据组uuid查组完整树路径")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		
		TeamVo teamVo = new TeamVo();
		teamVo.setUuid(jsonObj.getString("uuid"));
		List<TeamVo> teamList = teamService.getTreePathByTeamUuid(teamVo);
		JSONArray jsonList = new JSONArray();
		if (teamList != null && teamList.size() > 0) {
			JSONObject jsonObject = null;
			for (TeamVo tVo : teamList) {
				jsonObject = new JSONObject();
				jsonObject.put("uuid", tVo.getUuid());
				jsonList.add(jsonObject);
			}
		}
		return jsonList;
	}
}

