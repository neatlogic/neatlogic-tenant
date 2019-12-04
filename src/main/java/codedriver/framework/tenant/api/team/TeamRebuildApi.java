package codedriver.framework.tenant.api.team;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.service.TeamService;

@AuthAction(name="SYSTEM_TEAM_EDIT")
@Service
public class TeamRebuildApi extends ApiComponentBase{

	@Autowired
	private TeamService teamService;
	
	@Override
	public String getToken() {
		return "team/rebuild";
	}

	@Override
	public String getName() {
		return "重建前序排列接口";
	}
	
	@Override
	public String getConfig() {
		return null;
	}


	@Input({})
	@Output({})
	@Description(desc = "重建前序排列接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		teamService.rebuildLeftRightCode(null, 0);
		return null;
	}
}

