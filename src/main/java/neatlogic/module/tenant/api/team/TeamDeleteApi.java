package neatlogic.module.tenant.api.team;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.TEAM_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.exception.team.TeamNotFoundException;
import neatlogic.framework.lrcode.LRCodeManager;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.TeamService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@AuthAction(action = TEAM_MODIFY.class)
@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class TeamDeleteApi extends PrivateApiComponentBase {

    @Autowired
    private TeamMapper teamMapper;

    @Resource
    TeamService teamService;

    @Override
    public String getToken() {
        return "team/delete";
    }

    @Override
    public String getName() {
        return "nmtat.teamdeleteapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "uuid", type = ApiParamType.STRING, desc = "common.uuid", isRequired = true)})
    @Description(desc = "nmtat.teamdeleteapi.getname")
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
//        teamMapper.deleteTeamByUuidList(uuidList);
        teamMapper.updateTeamIsDeletedByUuidList(uuidList);
        teamMapper.deleteTeamUserByTeamUuidList(uuidList);
        teamMapper.deleteTeamRoleByTeamUuidList(uuidList);
        //delete teamUserTitle
        teamService.deleteTeamUserTitleByTeamUuid(uuid);
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
