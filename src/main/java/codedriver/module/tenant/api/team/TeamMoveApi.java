package codedriver.module.tenant.api.team;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.TeamService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-05 18:20
 **/
@Service
public class TeamMoveApi extends ApiComponentBase {

    private static final String TEAM_INNER = "inner";
    private static final String TEAM_NEXT = "next";
    private static final String TEAM_PREV = "prev";

    @Autowired
    private TeamService teamService;

    @Override
    public String getToken() {
        return "team/move";
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
             @Param( name = "moveType", type = ApiParamType.STRING, desc = "移动类型", isRequired = true),
             @Param( name = "targetUuid", type = ApiParamType.STRING, desc = "目标uuid", isRequired = true)})
    @Output({

    })
    @Description( desc = "组织架构移动接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        String moveType = jsonObj.getString("moveType");
        String targetUuid = jsonObj.getString("targetUuid");
        if (TEAM_INNER.equals(moveType)){
            teamService.moveTargetTeamInner(uuid, targetUuid);
        }
        if (TEAM_PREV.equals(moveType)){
            teamService.moveTargetTeamPrev(uuid, targetUuid);
        }
        if (TEAM_NEXT.equals(moveType)){
            teamService.moveTargetTeamNext(uuid, targetUuid);
        }
        return null;
    }
}
