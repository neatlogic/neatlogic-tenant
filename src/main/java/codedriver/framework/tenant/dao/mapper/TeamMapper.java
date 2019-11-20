package codedriver.framework.tenant.dao.mapper;

import java.util.List;

import codedriver.framework.tenant.dto.TeamVo;

public interface TeamMapper {

	public List<TeamVo> selectRoleTeamList(TeamVo teamVo);
	
	public List<TeamVo> selectTeamList(TeamVo teamVo);
	
}
