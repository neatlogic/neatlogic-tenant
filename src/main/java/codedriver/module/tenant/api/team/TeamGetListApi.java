package codedriver.module.tenant.api.team;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.TeamService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-20 10:43
 **/
@Service
public class TeamGetListApi extends ApiComponentBase {

    @Autowired
    private TeamService teamService;

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
            @Param( name = "teamIdList", desc = "用户组ID集合", type = ApiParamType.JSONARRAY, isRequired = true)
    })
    @Output({
            @Param( name = "teamList", desc = "用户组集合", explode = TeamVo[].class)
    })
    @Description(desc = "批量获取用户组信息接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        JSONArray teamIdArray = jsonObj.getJSONArray("teamIdList");
        List<TeamVo> teamList = new ArrayList<>();
        for (int i = 0; i < teamIdArray.size(); i++){
            String teamId = teamIdArray.getString(i);
            TeamVo teamVo = teamService.getTeamByUuid(teamId);
            teamList.add(teamVo);
        }
        returnObj.put("teamList", teamList);
        return returnObj;
    }
}
