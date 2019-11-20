package codedriver.framework.tenant.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.tenant.dao.mapper.TeamMapper;
import codedriver.framework.tenant.dto.TeamVo;

@Service
public class TeamServiceImpl implements TeamService{

	@Autowired
	TeamMapper teamMapper;
	
	@Override
	public List<TeamVo> selectRoleTeamList(TeamVo teamVo) {
		return teamMapper.selectRoleTeamList(teamVo);
	}

	@Override
	public List<TeamVo> selectTeamList(TeamVo teamVo) {
		return teamMapper.selectTeamList(teamVo);
	}

}
