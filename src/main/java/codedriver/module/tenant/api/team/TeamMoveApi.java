package codedriver.module.tenant.api.team;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Objects;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.exception.team.TeamMoveException;
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
             @Param( name = "sort", type = ApiParamType.INTEGER, desc = "sort(目标父级的位置，从0开始)", isRequired = true)})
    @Output({

    })
    @Description( desc = "组织架构移动接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
		teamMapper.getTeamLockByUuid(TeamVo.ROOT_UUID);
//		if(!teamService.checkLeftRightCodeIsExists()) {
//			teamService.rebuildLeftRightCode(TeamVo.ROOT_PARENTUUID, 0);
//		}
		teamService.rebuildLeftRightCode(TeamVo.ROOT_PARENTUUID, 0);
    	String uuid = jsonObj.getString("uuid");
        TeamVo team = teamMapper.getTeamByUuid(uuid);
        if(team == null) {
        	throw new TeamNotFoundException(uuid);
        }
        String parentUuid = jsonObj.getString("parentUuid");
        if(teamMapper.checkTeamIsExists(parentUuid) == 0) {
        	throw new TeamNotFoundException(parentUuid);
        }
        if(Objects.equal(uuid, parentUuid)) {
        	throw new TeamMoveException("移动后的父节点不可以是当前节点");
        }
        //判断移动后的父节点是否在当前节点的后代节点中
        if(teamMapper.checkTeamIsExistsByLeftRightCode(parentUuid, team.getLft(), team.getRht()) > 0) {
        	throw new TeamMoveException("移动后的父节点不可以是当前节点的后代节点");
        }
        Integer oldSort = team.getSort();
        int newSort = jsonObj.getIntValue("sort") + 1;
        if(parentUuid.equals(team.getParentUuid())) {
        	if(oldSort == newSort) {
        		return null;
        	}else if(oldSort > newSort) {//向上移动, 移动前后两个位置兄弟节点序号加一
        		teamMapper.updateSortIncrement(parentUuid, newSort, oldSort - 1);
        	}else {//向下移动, 移动前后两个位置兄弟节点序号减一
        		teamMapper.updateSortDecrement(parentUuid, oldSort + 1, newSort);
        	}
        }else {
        	//旧位置，被移动组后面的兄弟节点序号减一
        	teamMapper.updateSortDecrement(team.getParentUuid(), oldSort + 1, null);
			//新位置，被移动组后面的兄弟节点序号加一
        	teamMapper.updateSortIncrement(parentUuid, newSort, null);
        }
        team.setSort(newSort);
 		team.setParentUuid(parentUuid);
 		teamMapper.updateTeamSortAndParentUuid(team);
 		
 		//将被移动块中的所有节点的左右编码值设置到<=0
 		teamMapper.batchUpdateTeamLeftRightCodeByLeftRightCode(team.getLft(), team.getRht(), -team.getRht());
 		//计算被移动块右边的节点移动步长
 		int step = team.getRht() - team.getLft() + 1;
 		//更新旧位置右边的左右编码值
		teamMapper.batchUpdateTeamLeftCode(team.getLft(), -step);
		teamMapper.batchUpdateTeamRightCode(team.getLft(), -step);
		//找出被移动块移动后左编码值
		int lft = 0;
		if(newSort == 1) {
			TeamVo parentTeam = teamMapper.getTeamByUuid(parentUuid);
			lft = parentTeam.getLft() + 1;
 		}else {
 			TeamVo prevTeam = teamMapper.getTeamByParentUuidAndSort(parentUuid, newSort - 1);
 			lft = prevTeam.getRht() + 1;
 		}
		//更新新位置右边的左右编码值
		teamMapper.batchUpdateTeamLeftCode(lft, step);
		teamMapper.batchUpdateTeamRightCode(lft, step);
		
		//更新被移动块中节点的左右编码值
		teamMapper.batchUpdateTeamLeftRightCodeByLeftRightCode(team.getLft() - team.getRht(), team.getRht() - team.getRht(), lft - team.getLft() + team.getRht());
        return null;
    }
}
