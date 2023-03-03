/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.team;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.exception.team.TeamNotFoundException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-03-05 18:49
 **/
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class TeamTreeSearchApi extends PrivateApiComponentBase {

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
    	@Param(name = "uuid", type = ApiParamType.STRING, xss = true, desc = "主键ID"),
    	@Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "关键字")
    })
    @Output({
    	@Param(name = "children", type = ApiParamType.JSONARRAY, desc = "用户组织架构集合")
    })
    @Description(desc = "组织架构树检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	JSONObject resultObj = new JSONObject();   	
    	resultObj.put("children", new ArrayList<>());
    	List<TeamVo> teamList = new ArrayList<>();
    	String uuid = jsonObj.getString("uuid");
    	String keyword = jsonObj.getString("keyword");
		List<String> teamUuidList = new ArrayList<>();
    	Map<String, TeamVo> teamMap = new HashMap<>();
    	if(StringUtils.isNotBlank(uuid)) {
        	TeamVo teamVo = teamMapper.getTeamByUuid(uuid);
        	if(teamVo == null) {
        		throw new TeamNotFoundException(uuid);
        	}
        	teamList = teamMapper.getAncestorsAndSelfByLftRht(teamVo.getLft(), teamVo.getRht(), null);
        	for(TeamVo team : teamList) {
        		teamMap.put(team.getUuid(), team);
        		teamUuidList.add(team.getUuid());
        	}
    	}else if(StringUtils.isNotBlank(keyword)){
    		TeamVo keywordTeam = new TeamVo();
    		keywordTeam.setKeyword(keyword);
    		List<TeamVo> targetTeamList = teamMapper.searchTeam(keywordTeam);
    		for(TeamVo teamVo : targetTeamList) {
    			List<TeamVo> ancestorsAndSelf = teamMapper.getAncestorsAndSelfByLftRht(teamVo.getLft(), teamVo.getRht(), null);
    			for(TeamVo team : ancestorsAndSelf) {
    				if(!teamUuidList.contains(team.getUuid())) {
                		teamMap.put(team.getUuid(), team);
                		teamUuidList.add(team.getUuid());
                		teamList.add(team);
    				}
            	}
    		}
    	}else {
    		return resultObj;
    	}
    	
    	if(CollectionUtils.isNotEmpty(teamList)) {
    		TeamVo rootTeam = new TeamVo();
    		rootTeam.setUuid(TeamVo.ROOT_UUID);
    		rootTeam.setName("root");
    		rootTeam.setParentUuid(TeamVo.ROOT_PARENTUUID);
    		teamMap.put(TeamVo.ROOT_UUID, rootTeam);
//        	List<TeamVo> teamUserCountAndChildCountList = teamMapper.getTeamUserCountAndChildCountListByUuidList(teamUuidList,null);
//        	Map<String, TeamVo> teamUserCountAndChildCountMap = new HashMap<>();
//        	for(TeamVo team : teamUserCountAndChildCountList) {
//        		teamUserCountAndChildCountMap.put(team.getUuid(), team);
//        	}
			List<TeamVo> childCountList = teamMapper.getChildCountListByUuidList(teamUuidList);
			Map<String, Integer> childCountMap = new HashMap<>();
			for (TeamVo team : childCountList) {
				childCountMap.put(team.getUuid(), team.getChildCount());
			}
			List<TeamVo> teamUserCountList = teamMapper.getTeamUserCountListByUuidList(teamUuidList, null);
			Map<String, Integer> teamUserCountMap = new HashMap<>();
			for (TeamVo team : teamUserCountList) {
				teamUserCountMap.put(team.getUuid(), team.getUserCount());
			}
        	for(TeamVo team : teamList) {
        		TeamVo parentTeam = teamMap.get(team.getParentUuid());
        		if(parentTeam != null) {
        			team.setParent(parentTeam);
        		}
//        		TeamVo teamUserCountAndChildCount = teamUserCountAndChildCountMap.get(team.getUuid());
//        		if(teamUserCountAndChildCount != null) {
//            		team.setChildCount(teamUserCountAndChildCount.getChildCount());
//            		team.setUserCount(teamUserCountAndChildCount.getUserCount());
//        		}
				team.setChildCount(childCountMap.get(team.getUuid()));
				team.setUserCount(teamUserCountMap.get(team.getUuid()));
        	}
        	resultObj.put("children", rootTeam.getChildren());
    	}
    	return resultObj;
    }
}
