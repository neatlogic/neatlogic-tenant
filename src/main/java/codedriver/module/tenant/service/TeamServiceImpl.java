package codedriver.module.tenant.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;

@Service
@Transactional
public class TeamServiceImpl implements TeamService {

    private static final String DEFAULT_PARENTUUID = "0";

	@Autowired
	TeamMapper teamMapper;

	@Override
	public List<TeamVo> getTeamTree(TeamVo paramVo) {
		if (StringUtils.isBlank(paramVo.getParentUuid())){
			paramVo.setParentUuid(DEFAULT_PARENTUUID);
		}
		if (paramVo.getNeedPage()){
			int rowNum = teamMapper.searchTeamCount(paramVo);
			paramVo.setRowNum(rowNum);
			paramVo.setPageCount(PageUtil.getPageCount(rowNum, paramVo.getPageSize()));
		}
		return teamMapper.searchTeam(paramVo);
	}

    @Override
    public JSONArray getParentTeamTree(String uuid) {
	    JSONArray returnArray = new JSONArray();
	    returnArray.add(iterativeAssembly(uuid, null));
        return returnArray;
    }

	@Override
	public void moveTargetTeamInner(String uuid, String targetUuid) {
		TeamVo teamVo = new TeamVo();
		teamVo.setUuid(uuid);
		teamVo.setParentUuid(targetUuid);
		teamVo.setSort(1);
		teamMapper.updateTeamSortAndParentUuid(teamVo);
	}

	@Override
	public void moveTargetTeamPrev(String uuid, String targetUuid) {
		TeamVo targetTeam = teamMapper.getTeamByUuid(targetUuid);
		List<TeamVo> teamList = teamMapper.getTeamSortAfterTeamList(targetTeam.getParentUuid(), targetTeam.getSort());
		TeamVo team = new TeamVo();
		team.setUuid(uuid);
		team.setSort(targetTeam.getSort());
		team.setParentUuid(targetTeam.getParentUuid());
		teamMapper.updateTeamSortAndParentUuid(team);
		teamMapper.updateTeamSortAdd(targetUuid);
		for (TeamVo teamVo : teamList){
			teamMapper.updateTeamSortAdd(teamVo.getUuid());
		}
	}

	@Override
	public void moveTargetTeamNext(String uuid, String targetUuid) {
		TeamVo targetTeam = teamMapper.getTeamByUuid(targetUuid);
		List<TeamVo> teamList = teamMapper.getTeamSortAfterTeamList(targetTeam.getParentUuid(), targetTeam.getSort());
		TeamVo team = new TeamVo();
		team.setUuid(uuid);
		team.setSort(targetTeam.getSort() + 1);
		team.setParentUuid(targetTeam.getParentUuid());
		teamMapper.updateTeamSortAndParentUuid(team);

		if(teamList != null && teamList.size() > 0){
			for (TeamVo teamVo : teamList){
				teamMapper.updateTeamSortAdd(teamVo.getUuid());
			}
		}
	}

	public JSONObject iterativeAssembly(String uuid, JSONObject dataObj){
        TeamVo teamVo = teamMapper.getTeamByUuid(uuid);
        JSONObject teamObj = new JSONObject();
        teamObj.put("name", teamVo.getName());
        teamObj.put("uuid", teamVo.getUuid());
        teamObj.put("sort", teamVo.getSort());
        teamObj.put("parentUuid", teamVo.getParentUuid());
        teamObj.put("tagList", teamVo.getTagList());
        teamObj.put("userCount", teamVo.getUserCount());
        teamObj.put("childCount", teamVo.getChildCount());
        if (dataObj != null){
            JSONArray childArray = new JSONArray();
            childArray.add(dataObj);
            teamObj.put("children", childArray);
        }
        if (!DEFAULT_PARENTUUID.equals(teamVo.getParentUuid())){
         return iterativeAssembly(teamVo.getParentUuid(), teamObj);
        }
        return teamObj;
    }
}
