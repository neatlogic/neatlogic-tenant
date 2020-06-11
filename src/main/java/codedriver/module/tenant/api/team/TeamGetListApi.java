package codedriver.module.tenant.api.team;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
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
 * @create: 2020-03-20 10:43
 **/
@Service
public class TeamGetListApi extends ApiComponentBase {

    @Autowired
    private TeamMapper teamMapper;

    @Override
    public String getToken() {
        return "team/get/list";
    }

    @Override
    public String getName() {
        return "批量获取用户组信息接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param( name = "teamUuidList", desc = "用户组Uuid集合", type = ApiParamType.JSONARRAY, isRequired = true)
    })
    @Output({
            @Param( name = "teamList", desc = "用户组集合", explode = TeamVo[].class)
    })
    @Description(desc = "批量获取用户组信息接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        List<String> teamUuiddList = JSON.parseArray(jsonObj.getString("teamUuidList"), String.class);
        if(CollectionUtils.isNotEmpty(teamUuiddList)) {
            List<TeamVo> teamList = new ArrayList<>();
        	for(String teamUuid : teamUuiddList) {
        		TeamVo team = teamMapper.getTeamByUuid(teamUuid);
        		if(team == null) {
        			throw new TeamNotFoundException(teamUuid);
        		}
                teamList.add(team);
        	}
            returnObj.put("teamList", teamList);
        }
        return returnObj;
    }
}
