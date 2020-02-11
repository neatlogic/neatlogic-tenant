package codedriver.module.tenant.service;

import java.util.List;

import codedriver.framework.dto.TeamVo;

public interface TeamService {
	public List<TeamVo> searchTeam(TeamVo teamVo);

	public int deleteTeam(String teamUuid);

	public void saveTeam(TeamVo teamVo);

}
