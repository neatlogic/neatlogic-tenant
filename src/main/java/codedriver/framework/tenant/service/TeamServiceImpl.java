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
	
	@Override
	public List<TeamVo> searchTeamByName(TeamVo teamVo) {
		return teamMapper.searchTeamByName(teamVo);
	}

	@Override
	public TeamVo getTeamByUuid(String teamUuid) {
		return teamMapper.getTeamByUuid(teamUuid);
	}

	@Override
	public int insertTeam(TeamVo teamVo) {
		int right = 2;
		String parentId = teamVo.getParentId();
		if (parentId != null) {
			TeamVo parentTeam = teamMapper.selectTeamById(parentId);
			right = parentTeam.getRht();
			teamMapper.updateTeamRightForInsert(right);
			teamMapper.updateTeamLeftForInsert(right);
			teamVo.setLft(right);
			teamVo.setRht(right + 1);
		}
		teamMapper.insertTeam(teamVo);
		return 1;
	}

	@Override
	public int updateTeam(TeamVo teamVo) {
		teamMapper.updateTeam(teamVo);
		return 1;
	}
	
	@Override
	public void deleteTeam(String teamUuid) {
		TeamVo teamVo = teamMapper.selectTeamById(teamUuid);
		teamMapper.updateTeamLeftForDelete(teamVo);
		teamMapper.updateTeamRightForDelete(teamVo);
		teamMapper.deleteTeam(teamUuid);
	}
	
	@Override
	public Integer rebuildLeftRightCode(String parentId, Integer parentLft) {
		List<TeamVo> teamList = teamMapper.getTeamListByParentId(parentId);
		for (TeamVo team : teamList) {
			if (team.getChildCount() <= 0) {
				teamMapper.updateTeamLeftRightCode(team.getUuid(), parentLft + 1, parentLft + 2);
				parentLft += 2;
			} else {
				int lft = parentLft + 1;
				parentLft = rebuildLeftRightCode(team.getUuid(), lft);
				teamMapper.updateTeamLeftRightCode(team.getUuid(), lft, parentLft + 1);
				parentLft += 1;
			}
		}
		return parentLft;
	}
}
