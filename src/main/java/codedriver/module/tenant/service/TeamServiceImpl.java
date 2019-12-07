package codedriver.module.tenant.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;

@Service
@Transactional
public class TeamServiceImpl implements TeamService {

	@Autowired
	TeamMapper teamMapper;

	@Override
	public List<TeamVo> searchTeam(TeamVo teamVo) {
		if (teamVo.getNeedPage()) {
			int rowNum = teamMapper.searchTeamCount(teamVo);
			teamVo.setRowNum(rowNum);
			teamVo.setPageCount(PageUtil.getPageCount(rowNum, teamVo.getPageSize()));
		}
		return teamMapper.searchTeam(teamVo);
	}

	@Override
	public int deteteTeam(String teamUuid) {
		teamMapper.deleteUserTeamRoleByTeamUuid(teamUuid);
		teamMapper.deleteUserTeamByTeamUuid(teamUuid);
		teamMapper.deleteTeamByUuid(teamUuid);
		return 1;
	}
}
