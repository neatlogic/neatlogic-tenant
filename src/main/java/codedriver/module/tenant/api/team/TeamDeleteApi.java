package codedriver.module.tenant.api.team;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamUserVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.lock.core.LockManager;
import codedriver.framework.lrcode.LRCodeManager;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.auth.label.TEAM_MODIFY;
import codedriver.module.tenant.service.TeamService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AuthAction(action = TEAM_MODIFY.class)
@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class TeamDeleteApi extends PrivateApiComponentBase {

    @Autowired
    private TeamMapper teamMapper;

    @Override
    public String getToken() {
        return "team/delete";
    }

    @Override
    public String getName() {
        return "删除分组接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "uuid", type = ApiParamType.STRING, desc = "分组uuid", isRequired = true)})
    @Description(desc = "删除分组接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        TeamVo team = teamMapper.getTeamByUuid(uuid);
        if (team == null) {
            throw new TeamNotFoundException(uuid);
        }
        List<String> uuidList = teamMapper.getChildrenUuidListByLeftRightCode(team.getLft(), team.getRht());
        LRCodeManager.beforeDeleteTreeNode("team", "uuid", "parent_uuid", uuid);
        uuidList.add(team.getUuid());
        teamMapper.deleteTeamByUuidList(uuidList);
        teamMapper.deleteTeamUserByTeamUuidList(uuidList);
        teamMapper.deleteTeamRoleByTeamUuidList(uuidList);
        return null;
    }

//    private Object backup(JSONObject jsonObj){
//        lockService.getLockById("team");
//        if (teamMapper.checkLeftRightCodeIsWrong() > 0) {
//            teamService.rebuildLeftRightCode();
//        }
//        String uuid = jsonObj.getString("uuid");
//        TeamVo team = teamMapper.getTeamByUuid(uuid);
//        if (team == null) {
//            throw new TeamNotFoundException(uuid);
//        }
//        teamMapper.deleteTeamByLeftRightCode(team.getLft(), team.getRht());
//        // 计算被删除块右边的节点移动步长
//        int step = team.getRht() - team.getLft() + 1;
//        teamMapper.batchUpdateTeamLeftCode(team.getLft(), -step);
//        teamMapper.batchUpdateTeamRightCode(team.getLft(), -step);
//        return null;
//    }
}
