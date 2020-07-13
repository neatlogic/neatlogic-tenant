package codedriver.module.tenant.api.team;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-05 18:49
 **/
@Service
public class TeamTreeSearchApi extends ApiComponentBase {

    @Autowired
    private TeamMapper teamMapper;

    @Override
    public String getToken() {
        return "team/tree/search";
    }

    @Override
    public String getName() {
        return "组织架构树检索接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param( name = "uuid", desc = "主键ID", xss = true, type = ApiParamType.STRING),
            @Param( name = "keyword", desc = "关键字", xss = true, type = ApiParamType.STRING)
    })
    @Output({
            @Param(
                    name = "children",
                    type = ApiParamType.JSONARRAY,
                    desc = "用户组织架构集合")
    })
    @Description(desc = "组织架构树检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
//        String uuid = jsonObj.getString("uuid");
//        JSONObject returnObj = new JSONObject();
//        JSONArray returnArray = new JSONArray();
//	    returnArray.add(iterativeAssembly(uuid, null));
//        returnObj.put("children", returnArray);
//        return returnObj;
    	JSONObject resultObj = new JSONObject();   	
    	resultObj.put("children", new ArrayList<>());
    	String uuid = jsonObj.getString("uuid");
    	TeamVo teamVo = teamMapper.getTeamByUuid(uuid);
    	if(teamVo == null) {
    		throw new TeamNotFoundException(uuid);
    	}
    	List<TeamVo> teamList = teamMapper.getAncestorsAndSelfByLftRht(teamVo.getLft(), teamVo.getRht());
    	if(CollectionUtils.isNotEmpty(teamList)) {
    		List<String> teamUuidList = new ArrayList<>();
        	Map<String, TeamVo> teamMap = new HashMap<>();
        	for(TeamVo team : teamList) {
        		teamMap.put(team.getUuid(), team);
        		teamUuidList.add(team.getUuid());
        	}
        	List<TeamVo> teamUserCountAndChildCountList = teamMapper.getTeamUserCountAndChildCountListByUuidList(teamUuidList);
        	Map<String, TeamVo> teamUserCountAndChildCountMap = new HashMap<>();
        	for(TeamVo team : teamUserCountAndChildCountList) {
        		teamUserCountAndChildCountMap.put(team.getUuid(), team);
        	}
        	for(TeamVo team : teamList) {
        		TeamVo parentTeam = teamMap.get(team.getParentUuid());
        		if(parentTeam != null) {
        			team.setParent(parentTeam);
        		}
        		TeamVo teamUserCountAndChildCount = teamUserCountAndChildCountMap.get(team.getUuid());
        		if(teamUserCountAndChildCount != null) {
            		team.setChildCount(teamUserCountAndChildCount.getChildCount());
            		team.setUserCount(teamUserCountAndChildCount.getUserCount());
        		}
        	}
        	TeamVo rootTeam = teamMap.get(TeamVo.ROOT_UUID);
        	if(rootTeam != null) {
        		resultObj.put("children", rootTeam.getChildren());
        	}
    	}
    	return resultObj;
    }
    
//    public JSONObject iterativeAssembly(String uuid, JSONObject dataObj){
//        TeamVo teamVo = teamMapper.getTeamByUuid(uuid);
//        JSONObject teamObj = new JSONObject();
//        teamObj.put("name", teamVo.getName());
//        teamObj.put("uuid", teamVo.getUuid());
////        teamObj.put("sort", teamVo.getSort());
//        teamObj.put("parentUuid", teamVo.getParentUuid());
//        teamObj.put("tagList", teamVo.getTagList());
//        teamObj.put("userCount", teamVo.getUserCount());
//        teamObj.put("childCount", teamVo.getChildCount());
//        if (dataObj != null){
//            JSONArray childArray = new JSONArray();
//            childArray.add(dataObj);
//            teamObj.put("children", childArray);
//        }
//        if (!TeamVo.ROOT_UUID.equals(teamVo.getParentUuid())){
//         return iterativeAssembly(teamVo.getParentUuid(), teamObj);
//        }
//        return teamObj;
//    }
}
