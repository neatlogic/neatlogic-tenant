package neatlogic.module.tenant.api.team;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.TEAM_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dto.TeamUserVo;
import neatlogic.framework.exception.team.TeamNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-03-07 17:39
 **/
@Service
@AuthAction(action = TEAM_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class TeamUserSaveApi extends PrivateApiComponentBase {

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private UserService userService;

    @Override
    public String getToken() {
        return "team/user/save";
    }

    @Override
    public String getName() {
        return "分组用户保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "teamUuid", isRequired = true, desc = "分组uuid", type = ApiParamType.STRING),
            @Param(name = "userUuidList", desc = "用户Uuid集合", type = ApiParamType.JSONARRAY),
            @Param(name = "teamUuidList", desc = "分组Uuid集合", type = ApiParamType.JSONARRAY)
    })
    @Description(desc = "分组用户保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String teamUuid = jsonObj.getString("teamUuid");
        if (teamMapper.checkTeamIsExists(teamUuid) == 0) {
            throw new TeamNotFoundException(teamUuid);
        }
        List<String> userUuidList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("userUuidList")), String.class);
        List<String> teamUuidList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("teamUuidList")), String.class);
        Set<String> uuidList = userService.getUserUuidSetByUserUuidListAndTeamUuidList(userUuidList, teamUuidList);
        if (CollectionUtils.isNotEmpty(uuidList)) {
            teamMapper.deleteTeamUserByTeamUuid(teamUuid);
            for (String userUuid : uuidList) {
                teamMapper.insertTeamUser(new TeamUserVo(teamUuid, userUuid));
                //userService.updateUserCacheAndSessionByUserUuid(userUuid);
            }
        }
        return null;
    }
}
