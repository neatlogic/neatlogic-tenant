package codedriver.module.tenant.service;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;

@Service
public class TeamServiceImpl implements TeamService {

	@Autowired
	private TeamMapper teamMapper;

	@Override
	public void rebuildLeftRightCode() {
		rebuildLeftRightCode(TeamVo.ROOT_UUID, 1);
	}
	
	private Integer rebuildLeftRightCode(String parentUuid, Integer parentLft) {
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
		int count = teamMapper.getTeamCountOnLock();
		if(count == 0) {
			return true;
		}
		Integer maxRhtCode = teamMapper.getMaxRhtCode();
		if(Objects.equals(maxRhtCode, count * 2 + 1)) {
			return true;
		}
		return false;
	}

	@Override
	public TeamVo buildRootTeam() {
		Integer maxRhtCode = teamMapper.getMaxRhtCode();
		TeamVo rootTeam = new TeamVo();
		rootTeam.setUuid(TeamVo.ROOT_UUID);
		rootTeam.setName("root");
		rootTeam.setParentUuid(TeamVo.ROOT_PARENTUUID);
		rootTeam.setLft(1);
		rootTeam.setRht(maxRhtCode == null ? 2 : maxRhtCode + 1);
		return rootTeam;
	}

}
