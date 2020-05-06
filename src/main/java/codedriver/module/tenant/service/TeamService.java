package codedriver.module.tenant.service;

import java.util.List;

import com.alibaba.fastjson.JSONArray;

import codedriver.framework.dto.TeamVo;

public interface TeamService {
	public List<TeamVo> searchTeam(TeamVo teamVo);

	public TeamVo getTeam(TeamVo team,Integer isEdit);

	public int deleteTeam(String teamUuid);

	public void saveTeam(TeamVo teamVo);

	public void saveTeamUser(List<String> userIdList, String teamUuid);

	public List<TeamVo> getTeamTree(TeamVo teamVo);

	public JSONArray getParentTeamTree(String uuid);

	public void moveTargetTeamInner(String uuid, String targetUuid);

	public void moveTargetTeamPrev(String uuid, String targetUuid);

	public void moveTargetTeamNext(String uuid, String targetUuid);

}
