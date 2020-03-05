package codedriver.module.tenant.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import codedriver.framework.dto.TagVo;
import codedriver.module.tenant.util.UuidUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.regexp.internal.RE;
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
	public TeamVo getTeamByUuid(String teamUuid) {
		TeamVo teamVo = teamMapper.getTeamByUuid(teamUuid);
		int userCount = teamMapper.searchUserCountByTeamUuid(teamUuid);
		teamVo.setUserCount(userCount);
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

	@Override
	public void saveTeam(TeamVo teamVo) {
		if(teamVo.getUuid() != null){
			teamMapper.updateTeamNameByUuid(teamVo);
			teamMapper.deleteTeamTagByUuid(teamVo.getUuid());
		}else {
			if (teamVo.getParentUuid() == null){
				teamVo.setParentUuid("0");
			}
			int sort = teamMapper.getMaxTeamSortByParentUuid(teamVo.getParentUuid());
			teamVo.setSort(sort++);
			teamMapper.insertTeam(teamVo);
		}

		if (teamVo.getTagList() != null && teamVo.getTagList().size() > 0){
			for (TagVo tag : teamVo.getTagList()){
				teamVo.setTagId(tag.getId());
				teamMapper.insertTeamTag(teamVo);
			}
		}

	}

	@Override
	public JSONArray getTeamTree() {
		List<TeamVo> teamList = teamMapper.getTeamTree();
		Map<String, List<TeamVo>> map = new HashMap<>();
		for (TeamVo teamVo : teamList){
			if(map.containsKey(teamVo.getParentUuid())){
				map.get(teamVo.getParentUuid()).add(teamVo);
			}else {
				List<TeamVo> teams = new ArrayList<>();
				teams.add(teamVo);
				map.put(teamVo.getParentUuid(), teams);
			}
		}
		List<TeamVo> startList = map.get("0");
		if (startList != null && startList.size() > 0){
			return buildData(startList, map);
		}
		return null;
	}

	public JSONArray buildData(List<TeamVo> startList, Map<String, List<TeamVo>> map){
		JSONArray children = new JSONArray();
		for (TeamVo teamVo: startList){
			JSONObject teamObj = new JSONObject();
			teamObj.put("name", teamVo.getName());
			teamObj.put("uuid", teamVo.getUuid());
			teamObj.put("sort", teamVo.getSort());
			teamObj.put("parentUuid", teamVo.getParentUuid());
			teamObj.put("tagList", teamVo.getTagList());
			if (map.containsKey(teamVo.getUuid())){
				List<TeamVo> teams = map.get(teamVo.getUuid());
				teamObj.put("children", buildData(teams, map));
			}
			children.add(teamObj);
		}
		return children;
	}
}
