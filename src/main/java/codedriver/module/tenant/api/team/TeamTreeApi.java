package codedriver.module.tenant.api.team;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
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

    @Input({ @Param( name = "currentPage", desc = "当前页", type = ApiParamType.INTEGER),
             @Param( name = "needPage", desc = "是否分页", type = ApiParamType.BOOLEAN),
             @Param( name = "pageSize", desc = "每页最大数", type = ApiParamType.INTEGER)})
    @Output({
           @Param(
                   name = "children",
                   type = ApiParamType.JSONARRAY,
                   desc = "用户组织架构集合"),
           @Param( explode = BasePageVo.class)
    })
    @Description(desc = "用户组织架构树获取接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        TeamVo teamVo = new TeamVo();
        if (jsonObj.containsKey("needPage")){
            teamVo.setNeedPage(jsonObj.getBoolean("needPage"));
        }
        teamVo.setCurrentPage(jsonObj.getInteger("currentPage"));
        teamVo.setPageSize(jsonObj.getInteger("pageSize"));
        return teamService.getTeamTree(teamVo);
    }
}
