package codedriver.module.tenant.api.team;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.TeamService;

@AuthAction(name = "SYSTEM_TEAM_EDIT")
@Service
@Transactional
public class TeamDeleteApi extends ApiComponentBase {

	@Autowired
	private TeamMapper teamMapper;
    
    @Autowired
    private TeamService teamService;

	@Override
	public String getToken() {
		return "team/delete";
	}

	@Override
	public String getName() {
		return "删除分组接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "分组uuid", isRequired = true) 
	})
	@Description(desc = "删除分组接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		teamMapper.getTeamLockByUuid(TeamVo.ROOT_UUID);
		if(!teamService.checkLeftRightCodeIsExists()) {
			teamService.rebuildLeftRightCode(TeamVo.ROOT_PARENTUUID, 0);
		}
		String uuid = jsonObj.getString("uuid");
		TeamVo team = teamMapper.getTeamByUuid(uuid);
		if(team == null) {
			throw new TeamNotFoundException(uuid);
		}
		int count = teamMapper.getTeamCountByLeftRightCode(team.getLft(), team.getRht());
		teamMapper.deleteTeamByLeftRightCode(team.getLft(), team.getRht());
		teamMapper.batchUpdateTeamLeftCode(team.getLft(), -2 * count);
		teamMapper.batchUpdateTeamRightCode(team.getLft(), -2 * count);
//		iterativeDelete(uuid);
		return null;
	}
	
//	private void iterativeDelete(String teamUuid){
//		teamMapper.deleteUserTeamRoleByTeamUuid(teamUuid);
//		teamMapper.deleteUserTeamByTeamUuid(teamUuid);
//		teamMapper.deleteTeamTagByUuid(teamUuid);
//		teamMapper.deleteTeamByUuid(teamUuid);
//		List<TeamVo> childTeamList = teamMapper.getTeamByParentUuid(teamUuid);
//		if (childTeamList != null && childTeamList.size() > 0){
//			for (TeamVo childTeam : childTeamList){
//				iterativeDelete(childTeam.getUuid());
//			}
//		}
//	}
}
