package codedriver.framework.tenant.api.team;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.service.TeamService;

@AuthAction(name="SYSTEM_TEAM_EDIT")
@Service
public class TeamDeleteApi extends ApiComponentBase{

	@Autowired
	private TeamService teamService;
	
	@Override
	public String getToken() {
		return "team/delete";
	}

	@Override
	public String getName() {
		return "删除组信息";
	}
	
	@Override
	public String getConfig() {
		return null;
	}


	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "组id",isRequired=true)})
	@Output({})
	@Description(desc = "删除组信息")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String teamUuid = jsonObj.getString("uuid");		
		teamService.deleteTeam(teamUuid);
		return null;
	}
}

