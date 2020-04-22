package codedriver.module.tenant.api.team;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-05 18:20
 **/
@Service
public class TeamMoveApi extends ApiComponentBase {

    @Autowired
    private TeamMapper teamMapper;

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
             @Param( name = "parentUuid", type = ApiParamType.STRING, desc = "父uuid", isRequired = true),
             @Param( name = "sort", type = ApiParamType.INTEGER, desc = "sort", isRequired = true)})
    @Output({

    })
    @Description( desc = "组织架构移动接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        TeamVo team = teamMapper.getTeamByUuid(uuid);
        if(team == null) {
        	throw new TeamNotFoundException(uuid);
        }
        String parentUuid = jsonObj.getString("parentUuid");
        Integer sort = jsonObj.getInteger("sort");
        List<TeamVo> teamList = teamMapper.getTeamSortAfterTeamList(parentUuid, sort);
		team.setUuid(uuid);
		team.setSort(sort);
		team.setParentUuid(parentUuid);
		teamMapper.updateTeamSortAndParentUuid(team);
		for (TeamVo teamVo : teamList){
			teamMapper.updateTeamSortAdd(teamVo.getUuid());
		}
        return null;
    }
}
