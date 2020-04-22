package codedriver.module.tenant.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import codedriver.framework.dto.TagVo;
import codedriver.framework.file.core.IFileTypeHandler;
import codedriver.module.tenant.util.UuidUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	public List<TeamVo> searchTeam(TeamVo teamVo) {
		if (teamVo.getNeedPage()) {
			int rowNum = teamMapper.searchTeamCount(teamVo);
			teamVo.setRowNum(rowNum);
			teamVo.setPageCount(PageUtil.getPageCount(rowNum, teamVo.getPageSize()));
		}
		return teamMapper.searchTeam(teamVo);
	}

	@Override
	public TeamVo getTeam(TeamVo team) {
		TeamVo teamVo = teamMapper.getTeam(team);
		if (StringUtils.isNotBlank(team.getUuid())){
			int userCount = teamMapper.searchUserCountByTeamUuid(team.getUuid());
			teamVo.setUserCount(userCount);
			List<String> pathNameList = new ArrayList<>();
			getTeamPath(teamVo, pathNameList);
			teamVo.setPathNameList(pathNameList);
		}
		return teamVo;
	}

	@Override
	public int deleteTeam(String teamUuid) {
		iterativeDelete(teamUuid);
		return 1;
	}

	public void iterativeDelete(String teamUuid){
		teamMapper.deleteUserTeamRoleByTeamUuid(teamUuid);
		teamMapper.deleteUserTeamByTeamUuid(teamUuid);
		teamMapper.deleteTeamTagByUuid(teamUuid);
		teamMapper.deleteTeamByUuid(teamUuid);
		List<TeamVo> childTeamList = teamMapper.getTeamByParentUuid(teamUuid);
		if (childTeamList != null && childTeamList.size() > 0){
			for (TeamVo childTeam : childTeamList){
				iterativeDelete(childTeam.getUuid());
			}
		}
	}

	public void getTeamPath(TeamVo teamVo, List<String> pathNameList){
		if (!teamVo.getParentUuid().equals("0")){
			TeamVo parentTeam = teamMapper.getTeamByUuid(teamVo.getParentUuid());
			getTeamPath(parentTeam, pathNameList);
		}
		pathNameList.add(teamVo.getName());
	}

	@Override
	public void saveTeam(TeamVo teamVo) {
		if(teamMapper.getTeamByUuid(teamVo.getUuid()) != null){
			teamMapper.updateTeamNameByUuid(teamVo);
			teamMapper.deleteTeamTagByUuid(teamVo.getUuid());
		}else {
			if (teamVo.getParentUuid() == null){
				teamVo.setParentUuid(DEFAULT_PARENTUUID);
			}
			int sort = 0;
			List<TeamVo> teamList = teamMapper.getTeamByParentUuid(teamVo.getParentUuid());
			if (teamList != null && teamList.size() > 0){
				sort = teamMapper.getMaxTeamSortByParentUuid(teamVo.getParentUuid());
			}
			sort++;
			teamVo.setSort(sort);
			teamMapper.insertTeam(teamVo);
			if (CollectionUtils.isNotEmpty(teamVo.getUserIdList())){
				for (String userId : teamVo.getUserIdList()){
					teamMapper.insertTeamUser(teamVo.getUuid(), userId);
				}
			}
		}

		if (teamVo.getTagList() != null && teamVo.getTagList().size() > 0){
			for (TagVo tag : teamVo.getTagList()){
				teamVo.setTagId(tag.getId());
				teamMapper.insertTeamTag(teamVo);
			}
		}

	}

	@Override
	public void saveTeamUser(List<String> userIdList, String teamUuid) {
		teamMapper.deleteUserTeamByTeamUuid(teamUuid);
		if (CollectionUtils.isNotEmpty(userIdList)){
			for (String userId: userIdList){
				teamMapper.insertTeamUser(teamUuid, userId);
			}
		}

	}

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
