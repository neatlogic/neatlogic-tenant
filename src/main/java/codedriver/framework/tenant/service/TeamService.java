package codedriver.framework.tenant.service;

import java.util.List;

import codedriver.framework.dto.TeamVo;

public interface TeamService {
	
	public List<TeamVo> selectTeamList(TeamVo teamVo);
	
	public int updateTeamRole(List<TeamVo> teamList, String roleName);
		
	public List<TeamVo> searchTeamByName(TeamVo teamVo);
	
	public TeamVo getTeamByUuid(String teamUuid);
	
	public int insertTeam(TeamVo teamVo);
	
	public int updateTeam(TeamVo teamVo);

	public void deleteTeam(String teamUuid);
	
	public Integer rebuildLeftRightCode(String parentId, Integer parentLft);
	
	public int moveTeam(String parentId, String teamUuid, Integer lft, Integer rht, String targetTeamUuid, String moveType);

}
