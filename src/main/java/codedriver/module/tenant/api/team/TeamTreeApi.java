package codedriver.module.tenant.api.team;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.TeamService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeamTreeApi extends ApiComponentBase {

    @Autowired
    private TeamService teamService;

    @Override
    public String getToken() {
        return "team/tree";
    }

    @Override
    public String getName() {
        return "用户组织架构树获取接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({
           @Param(
                   name = "children",
                   type = ApiParamType.JSONARRAY,
                   desc = "用户组织架构集合")
    })
    @Description(desc = "用户组织架构树获取接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        returnObj.put("children", teamService.getTeamTree());
        return returnObj;
    }
}
