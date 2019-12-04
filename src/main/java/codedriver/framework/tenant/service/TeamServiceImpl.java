package codedriver.framework.tenant.service;

import java.util.ArrayList;
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
	public List<TeamVo> getTreePathByTeamUuid(TeamVo teamVo) {
		return teamMapper.getTreePathByTeamUuid(teamVo);
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
	
	@Override
	public int moveTeam(String parentId, String teamUuiId, Integer lft, Integer rht, String targetTeamUuid, String moveType) {
		TeamVo teamVo = teamMapper.selectTeamById(teamUuiId);
		if (!teamVo.getLft().equals(lft) || !teamVo.getRht().equals(rht)) {// 位置发生变动，不允许更新
			return 0;
		}
		TeamVo targetTeamVo = teamMapper.selectTeamById(targetTeamUuid);
		List<TeamVo> teamList = teamMapper.getTeamsByLeftRight(teamVo);
		List<String> teamIdList = new ArrayList<String>();
		int diff = teamVo.getRht() - teamVo.getLft() + 1;
		int min = 1, max = 1, teamDiff = 0;
		if (teamVo.getLft() > targetTeamVo.getRht()) {// 原来在目标右边
			if ("inner".equals(moveType)) {
				min = targetTeamVo.getLft() + 1;
				max = teamVo.getLft();
				teamDiff = targetTeamVo.getLft() + 1 - teamVo.getLft();
			} else if ("next".equals(moveType)) {
				min = targetTeamVo.getRht() + 1;
				max = teamVo.getRht();
				teamDiff = targetTeamVo.getRht() + 1 - teamVo.getLft();
			} else if ("prev".equals(moveType)) {
				min = targetTeamVo.getLft();
				max = teamVo.getRht();
				teamDiff = targetTeamVo.getLft() - teamVo.getLft();
			}
		} else if (teamVo.getRht() < targetTeamVo.getLft()) {// 原来在目标左边
			if ("inner".equals(moveType)) {
				min = teamVo.getLft();
				max = targetTeamVo.getLft();
				teamDiff = targetTeamVo.getLft() - diff + 1 - teamVo.getLft();
			} else if ("next".equals(moveType)) {
				min = teamVo.getLft();
				max = targetTeamVo.getRht();
				teamDiff = targetTeamVo.getRht() - diff + 1 - teamVo.getLft();
			} else if ("prev".equals(moveType)) {
				min = teamVo.getLft();
				max = targetTeamVo.getLft() - 1;
				teamDiff = targetTeamVo.getLft() - diff - teamVo.getLft();
			}
		} else {// 原来是目标子树
			if ("inner".equals(moveType)) {
				min = targetTeamVo.getLft() + 1;
				max = teamVo.getLft();
				teamDiff = targetTeamVo.getLft() - teamVo.getLft() + 1;
			} else if ("next".equals(moveType)) {
				min = teamVo.getLft();
				max = targetTeamVo.getRht();
				teamDiff = targetTeamVo.getRht() - diff + 1 - teamVo.getLft();
			} else if ("prev".equals(moveType)) {
				min = targetTeamVo.getLft();
				max = teamVo.getRht();
				teamDiff = targetTeamVo.getLft() - teamVo.getLft();
			}
		}

		for (TeamVo t : teamList) {
			teamIdList.add(t.getUuid());
		}

		if (!parentId.equals(teamVo.getParentId())) {
			teamMapper.updateTeamParentId(teamVo.getUuid(), parentId);
		}

		if (teamVo.getRht() < targetTeamVo.getLft() || (teamVo.getLft() > targetTeamVo.getLft() && teamVo.getRht() < targetTeamVo.getRht() && moveType.equals("next"))) {
			diff = -diff;
		}

		teamMapper.updateTeamLeftByLeft(diff, min, max, teamIdList);
		teamMapper.updateTeamRightByRight(diff, min, max, teamIdList);
		teamMapper.updateTeamLeftRight(teamDiff, teamIdList);
		return 1;
	}
}
