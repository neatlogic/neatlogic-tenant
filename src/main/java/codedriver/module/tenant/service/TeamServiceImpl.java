package codedriver.module.tenant.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.exception.team.TeamNotFoundException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamServiceImpl implements TeamService {

	@Autowired
	private TeamMapper teamMapper;

	public Integer rebuildLeftRightCode(String parentUuid, Integer parentLft) {
		List<TeamVo> teamList;
		if(TeamVo.ROOT_PARENTUUID.equals(parentUuid)){
			teamList = new ArrayList<>();
			TeamVo vo = buildRootTeam();
			List<TeamVo> teamVoListForRoot = teamMapper.getTeamByParentUuid("0");
			vo.setChildCount(teamVoListForRoot.isEmpty() ? 0 : teamVoListForRoot.size());
			teamList.add(vo);
		}else{
			teamList = teamMapper.getTeamByParentUuid(parentUuid);
		}
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
//		int count = teamMapper.searchTeamCount(new TeamVo());
//		TeamVo rootTeam = teamMapper.getTeamByUuid(TeamVo.ROOT_UUID);
//		if(rootTeam == null) {
//			throw new TeamNotFoundException(TeamVo.ROOT_UUID);
//		}
		int count = 0;
		count = teamMapper.getTeamCountOnLock();
		Integer maxRhtCode = teamMapper.getMaxRhtCode();
		if(maxRhtCode != null){
			if(Objects.equals(maxRhtCode.intValue(), count * 2 + 1) || count == 0) {
				return true;
			}
		}
//		if(Objects.equals(rootTeam.getLft(), 1) && Objects.equals(rootTeam.getRht(), count * 2)) {
//			return true;
//		}
		return false;
	}

	@Override
	public TeamVo buildRootTeam() {
		Integer maxRhtCode = teamMapper.getMaxRhtCode();
		TeamVo rootTeam = new TeamVo();
		rootTeam.setUuid("0");
		rootTeam.setName("root");
		rootTeam.setParentUuid("-1");
		rootTeam.setLft(1);
		rootTeam.setRht(maxRhtCode == null ? 2 : maxRhtCode.intValue() + 1);
		return rootTeam;
	}

//	@Override
//	public void recursiveDeleteTeam(String parentUuid) {
//		List<TeamVo> teamList = teamMapper.getTeamByParentUuid(parentUuid);
//		if(teamList.size() == 0){
//			teamMapper.deleteTeamByUuid(parentUuid);
//		}else{
//			teamMapper.deleteTeamByUuid(parentUuid);
//			for(TeamVo vo : teamList){
//				recursiveDeleteTeam(vo.getUuid());
//			}
//		}
//	}
//
//	@Override
//	public boolean checkTeamIsExistsByUuid(String uuid,String targetUuid) {
//		List<TeamVo> teamList = teamMapper.getTeamByParentUuid(uuid);
//		for(TeamVo vo : teamList){
//			if(targetUuid.equals(vo.getUuid())){
//				return true;
//			}
//			checkTeamIsExistsByUuid(vo.getUuid(),targetUuid);
//		}
//		return false;
//	}
}
