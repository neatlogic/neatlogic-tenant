package codedriver.module.tenant.service;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.exception.team.TeamNotFoundException;

@Service
public class TeamServiceImpl implements TeamService {

	@Autowired
	private TeamMapper teamMapper;
	
	public Integer rebuildLeftRightCode(String parentUuid, Integer parentLft) {
		List<TeamVo> teamList = teamMapper.getTeamByParentUuid(parentUuid);
		for(TeamVo team : teamList) {
			if(team.getChildCount() == 0) {
				teamMapper.updateTeamLeftRightCode(team.getUuid(), parentLft + 1, parentLft + 2);
				parentLft += 2;
			}else {
				int lft = parentLft + 1;
				parentLft = rebuildLeftRightCode(team.getUuid(), lft);
				teamMapper.updateTeamLeftRightCode(team.getUuid(), lft, parentLft + 1);
				parentLft += 1;
			}
		}
		return parentLft;
	}

	@Override
	public boolean checkLeftRightCodeIsExists() {
		int count = teamMapper.searchTeamCount(new TeamVo());
		TeamVo rootTeam = teamMapper.getTeamByUuid(TeamVo.ROOT_UUID);
		if(rootTeam == null) {
			throw new TeamNotFoundException(TeamVo.ROOT_UUID);
		}
		if(Objects.equals(rootTeam.getLft(), 1) && Objects.equals(rootTeam.getRht(), count * 2)) {
			return true;
		}
		return false;
	}
}
