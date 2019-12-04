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
public class TeamMoveApi extends ApiComponentBase{

	@Autowired
	private TeamService teamService;
	
	@Override
	public String getToken() {
		return "team/move";
	}

	@Override
	public String getName() {
		return "移动组接口";
	}
	
	@Override
	public String getConfig() {
		return null;
	}


	@Input({ @Param(name = "teamUuid", type = ApiParamType.STRING, desc = "组id",isRequired=true),
		@Param(name = "parentId", type = ApiParamType.STRING, desc = "父级组id",isRequired=true),
		@Param(name = "lft", type = ApiParamType.INTEGER, desc = "左编码",isRequired=true),
		@Param(name = "rht", type = ApiParamType.INTEGER, desc = "右编码",isRequired=true),
		@Param(name = "targetTeamUuid", type = ApiParamType.STRING, desc = "目标组id",isRequired=true),
		@Param(name = "movetype", type = ApiParamType.STRING, desc = "移动类型(inner:移动到某个组内;   next:移动到某个组的下一个相邻节点;  prev:移动到某个组的上一个相邻节点)",isRequired=true)		
	})
	@Output({})
	@Description(desc = "移动组接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String parentId = jsonObj.getString("parentId");
		String teamUuid = jsonObj.getString("teamUuid");
		int lft = jsonObj.getIntValue("lft");
		int rht = jsonObj.getIntValue("rht");
		String targetTeamUuid = jsonObj.getString("targetTeamUuid");
		String movetype = jsonObj.getString("movetype");
		teamService.moveTeam(parentId, teamUuid, lft, rht, targetTeamUuid, movetype);
		return null;
	}
}

