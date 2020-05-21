package codedriver.module.tenant.api.team;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.TeamService;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-05 18:20
 **/
@Service
@Transactional
public class TeamMoveApi extends ApiComponentBase {

    @Autowired
    private TeamMapper teamMapper;
    
    @Autowired
    private TeamService teamService;

    @Override
    public String getToken() {
        return "team/tree/move";
    }

    @Override
    public String getName() {
        return "组织架构移动接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "uuid", type = ApiParamType.STRING, desc = "组uuid", isRequired = true),
             @Param( name = "parentUuid", type = ApiParamType.STRING, desc = "父uuid", isRequired = true,minLength = 1),
             @Param( name = "sort", type = ApiParamType.INTEGER, desc = "sort", isRequired = true)})
    @Output({

    })
    @Description( desc = "组织架构移动接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
		teamMapper.getTeamLockByUuid(TeamVo.ROOT_UUID);
		if(!teamService.checkLeftRightCodeIsExists()) {
			teamService.rebuildLeftRightCode(TeamVo.ROOT_PARENTUUID, 0);
		}
    	String uuid = jsonObj.getString("uuid");
        TeamVo team = teamMapper.getTeamByUuid(uuid);
        if(team == null) {
        	throw new TeamNotFoundException(uuid);
        }
        Integer sort = team.getSort();
        List<TeamVo> teamAddList = null;
        List<TeamVo> teamDescList = null;
        int targetSort = jsonObj.getIntValue("sort");
        String parentUuid = jsonObj.getString("parentUuid");
        if(parentUuid.equals(team.getParentUuid())) {
        	if(sort == targetSort) {
        		return null;
        	}else if(sort > targetSort) {//向上移动
        		teamAddList = teamMapper.getTeamSortUpTeamList(parentUuid, sort,targetSort);
        	}else {//向下移动
        		teamDescList = teamMapper.getTeamSortDownTeamList(parentUuid, sort,targetSort);
        	}
        }else {
        	teamAddList = teamMapper.getTeamSortAfterTeamList(parentUuid, targetSort);
        	teamDescList = teamMapper.getTeamSortAfterTeamList(team.getParentUuid(), sort+1);
        }
        team.setSort(targetSort);
 		team.setParentUuid(parentUuid);
 		teamMapper.updateTeamSortAndParentUuid(team);
 		if(CollectionUtils.isNotEmpty(teamAddList)) {
	 		for (TeamVo teamTmp : teamAddList){
	 			teamMapper.updateTeamSortAdd(teamTmp.getUuid());
	 		}
 		}
 		if(CollectionUtils.isNotEmpty(teamDescList)) {
 			for (TeamVo teamTmp : teamDescList){
     			teamMapper.updateTeamSortDec(teamTmp.getUuid());
     		}
 		}
        return null;
    }
}
