package codedriver.module.tenant.api.team;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.lock.service.LockManager;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.auth.label.TEAM_MODIFY;
import codedriver.module.tenant.exception.team.TeamMoveException;
import codedriver.module.tenant.service.TeamService;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-05 18:20
 **/
@Service
@Transactional
@AuthAction(action = TEAM_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class TeamMoveApi extends PrivateApiComponentBase {

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private TeamService teamService;

    @Autowired
    private LockManager lockService;

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

    @Input({@Param(name = "uuid", type = ApiParamType.STRING, desc = "组uuid", isRequired = true),
        @Param(name = "parentUuid", type = ApiParamType.STRING, desc = "父uuid", isRequired = true, minLength = 1),
        @Param(name = "sort", type = ApiParamType.INTEGER, desc = "sort(目标父级的位置，从0开始)", isRequired = true)})
    @Output({

    })
    @Description(desc = "组织架构移动接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        lockService.getLockById("team");
        if (teamMapper.checkLeftRightCodeIsWrong() > 0) {
            teamService.rebuildLeftRightCode();
        }
        String uuid = jsonObj.getString("uuid");
        TeamVo team = teamMapper.getTeamByUuid(uuid);
        if (team == null) {
            throw new TeamNotFoundException(uuid);
        }
        String parentUuid = jsonObj.getString("parentUuid");
        TeamVo parentTeam = new TeamVo();
        if (TeamVo.ROOT_UUID.equals(parentUuid)) {
            parentTeam.setUuid(TeamVo.ROOT_UUID);
            parentTeam.setName("root");
            parentTeam.setParentUuid(TeamVo.ROOT_PARENTUUID);
            parentTeam.setLft(1);
        } else {
            parentTeam = teamMapper.getTeamByUuid(parentUuid);
            if (parentTeam == null) {
                throw new TeamNotFoundException(parentUuid);
            }
        }
        if (Objects.equal(uuid, parentUuid)) {
            throw new TeamMoveException("移动后的父节点不可以是当前节点");
        }

        if (!parentUuid.equals(team.getParentUuid())) {
            // 判断移动后的父节点是否在当前节点的后代节点中
            if (teamMapper.checkTeamIsExistsByLeftRightCode(parentUuid, team.getLft(), team.getRht()) > 0) {
                throw new TeamMoveException("移动后的父节点不可以是当前节点的后代节点");
            }

            team.setParentUuid(parentUuid);
            teamMapper.updateTeamParentUuidByUuid(team);
        }

        // 将被移动块中的所有节点的左右编码值设置为<=0
        teamMapper.batchUpdateTeamLeftRightCodeByLeftRightCode(team.getLft(), team.getRht(), -team.getRht());
        // 计算被移动块右边的节点移动步长
        int step = team.getRht() - team.getLft() + 1;
        // 更新旧位置右边的左右编码值
        teamMapper.batchUpdateTeamLeftCode(team.getLft(), -step);
        teamMapper.batchUpdateTeamRightCode(team.getLft(), -step);

        // 找出被移动块移动后左编码值
        int lft = 0;
        int sort = jsonObj.getIntValue("sort");
        if (sort == 0) {// 移动到首位
            lft = parentTeam.getLft() + 1;
        } else {
            TeamVo prevTeam = teamMapper.getTeamByParentUuidAndStartNum(parentUuid, sort);
            lft = prevTeam.getRht() + 1;
        }

        // 更新新位置右边的左右编码值
        teamMapper.batchUpdateTeamLeftCode(lft, step);
        teamMapper.batchUpdateTeamRightCode(lft, step);

        // 更新被移动块中节点的左右编码值
        teamMapper.batchUpdateTeamLeftRightCodeByLeftRightCode(team.getLft() - team.getRht(),
            team.getRht() - team.getRht(), lft - team.getLft() + team.getRht());
        return null;
    }
}
