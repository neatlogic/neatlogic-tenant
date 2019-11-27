package codedriver.framework.tenant.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codedriver.framework.dto.TeamVo;
import codedriver.framework.tenant.dao.mapper.TeamMapper;


@Service
@Transactional
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

	@Override
	public int updateTeamRole(List<TeamVo> teamList, String roleName) {
		this.teamMapper.deleteTeamRoleByRoleName(roleName);
		int count = 0;
		if (teamList.size() > 0) {
			count = teamMapper.insertTeamRole(teamList);
		}
		return count;
	}

}
