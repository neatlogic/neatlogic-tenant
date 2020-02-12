package codedriver.module.tenant.api.team;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeamCountApi extends ApiComponentBase {

    @Autowired
    private TeamMapper teamMapper;

    @Override
    public String getToken() {
        return "team/count";
    }

    @Override
    public String getName() {
        return "成员组数目统计接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({
            @Param(name = "teamCount", type = ApiParamType.INTEGER, desc = "成员组数目")
    })
    @Description(desc = "成员组统计接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        int count = teamMapper.searchTeamCount(new TeamVo());
        returnObj.put("teamCount", count);
        return returnObj;
    }
}
