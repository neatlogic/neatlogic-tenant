package codedriver.module.tenant.api.team;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.TEAM_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.TeamLevel;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.*;
import codedriver.framework.exception.team.TeamLevelNotFoundException;
import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.lrcode.LRCodeManager;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.TeamService;
import codedriver.module.tenant.service.UserService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@AuthAction(action = TEAM_MODIFY.class)

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class TeamSaveApi extends PrivateApiComponentBase {

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;
    @Resource
    private TeamService teamService;

    @Override
    public String getToken() {
        return "team/save";
    }

    @Override
    public String getName() {
        return "保存组信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "组id", isRequired = false),
            @Param(name = "name", type = ApiParamType.STRING, desc = "组名", isRequired = true, xss = true),
            @Param(name = "parentUuid", type = ApiParamType.STRING, desc = "父级组id"),
            @Param(name = "level", type = ApiParamType.STRING, desc = "层级"),
            @Param(name = "userUuidList", type = ApiParamType.JSONARRAY, desc = "用户uuid集合"),
            @Param(name = "teamUuidList", type = ApiParamType.JSONARRAY, desc = "分组uuid集合"),
            @Param(name = "teamUserTitleList", type = ApiParamType.JSONARRAY, desc = "分组领导集合")
    })
    @Output({@Param(name = "uuid", type = ApiParamType.STRING, desc = "保存的组id")})
    @Description(desc = "保存组信息")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String level = jsonObj.getString("level");
        List<TeamUserTitleVo> teamUserTitleList = null;
        if (CollectionUtils.isNotEmpty(jsonObj.getJSONArray("teamUserTitleList"))) {
            teamUserTitleList = jsonObj.getJSONArray("teamUserTitleList").toJavaList(TeamUserTitleVo.class);
        }
        if (StringUtils.isNotBlank(level) && TeamLevel.getValue(level) == null) {
            throw new TeamLevelNotFoundException(level);
        }
        String uuid = jsonObj.getString("uuid");
        TeamVo teamVo = new TeamVo();
        teamVo.setName(jsonObj.getString("name"));
        teamVo.setLevel(level);
        teamVo.setUuid(uuid);
        if (StringUtils.isNotBlank(uuid)) {
            if (teamMapper.checkTeamIsExists(uuid) == 0) {
                throw new TeamNotFoundException(uuid);
            }
            teamVo.setUuid(uuid);
            teamMapper.updateTeamNameByUuid(teamVo);
            teamService.deleteTeamUserTitleByTeamUuid(uuid);
        } else {
            String parentUuid = jsonObj.getString("parentUuid");
            if (StringUtils.isBlank(parentUuid)) {
                parentUuid = TeamVo.ROOT_UUID;
            } else if (!TeamVo.ROOT_UUID.equals(parentUuid)) {
                TeamVo parentTeam = teamMapper.getTeamByUuid(parentUuid);
                if (parentTeam == null) {
                    throw new TeamNotFoundException(parentUuid);
                }
            }
            teamVo.setParentUuid(parentUuid);
            int lft = LRCodeManager.beforeAddTreeNode("team", "uuid", "parent_uuid", parentUuid);
            teamVo.setLft(lft);
            teamVo.setRht(lft + 1);

            List<String> upwardTeamUuidList = new ArrayList<>();
            List<String> upwardTeamNameList = new ArrayList<>();
            List<TeamVo> upwardTeamList = teamMapper.getAncestorsAndSelfByLftRht(lft, lft + 1, null);
            for (TeamVo upwardTeam : upwardTeamList) {
                upwardTeamUuidList.add(upwardTeam.getUuid());
                upwardTeamNameList.add(upwardTeam.getName());
            }
            upwardTeamUuidList.add(teamVo.getUuid());
            upwardTeamNameList.add(teamVo.getName());
            teamVo.setUpwardUuidPath(String.join(",", upwardTeamUuidList));
            teamVo.setUpwardNamePath(String.join("/", upwardTeamNameList));
            teamMapper.insertTeam(teamVo);
            List<String> userUuidList = JSON.parseArray(jsonObj.getString("userUuidList"), String.class);
            List<String> teamUuidList = JSON.parseArray(jsonObj.getString("teamUuidList"), String.class);
            Set<String> uuidList = userService.getUserUuidSetByUserUuidListAndTeamUuidList(userUuidList, teamUuidList);
            if (CollectionUtils.isNotEmpty(uuidList)) {
                for (String userUuid : uuidList) {
                    teamMapper.insertTeamUser(new TeamUserVo(teamVo.getUuid(), userUuid));
                }
            }
        }
        //insert teamUserTitle
        if (CollectionUtils.isNotEmpty(teamUserTitleList)) {
            List<UserTitleVo> userTitleVoList = userMapper.getUserTitleListLockByTitleNameList(teamUserTitleList.stream().map(TeamUserTitleVo::getTitle).collect(Collectors.toList()));
            Map<String, Long> userTitleMap = new HashMap<>();
            for (UserTitleVo userTitleVo : userTitleVoList) {
                userTitleMap.put(userTitleVo.getName(), userTitleVo.getId());
            }
            for (TeamUserTitleVo teamUserTitleVo : teamUserTitleList) {
                Long titleId;
                if (!userTitleMap.containsKey(teamUserTitleVo.getTitle())) {
                    UserTitleVo userTitleVo = new UserTitleVo(teamUserTitleVo.getTitle());
                    userMapper.insertUserTitle(userTitleVo);
                    titleId = userTitleVo.getId();
                } else {
                    titleId = userTitleMap.get(teamUserTitleVo.getTitle());
                }
                for (String userUuid : teamUserTitleVo.getUserList()) {
                    teamMapper.insertTeamUserTitle(teamVo.getUuid(), userUuid, titleId);
                }
            }
        }

        JSONObject returnObj = new JSONObject();
        returnObj.put("uuid", teamVo.getUuid());
        return returnObj;
    }

//    private Object backup(JSONObject jsonObj){
//        JSONObject returnObj = new JSONObject();
//        String level = jsonObj.getString("level");
//        if (StringUtils.isNotBlank(level) && TeamLevel.getValue(level) == null) {
//            throw new TeamLevelNotFoundException(level);
//        }
//        String uuid = jsonObj.getString("uuid");
//        TeamVo teamVo = new TeamVo();
//        teamVo.setName(jsonObj.getString("name"));
//        teamVo.setLevel(level);
//        if (StringUtils.isNotBlank(uuid)) {
//            if (teamMapper.checkTeamIsExists(uuid) == 0) {
//                throw new TeamNotFoundException(uuid);
//            }
//            teamVo.setUuid(uuid);
//            teamMapper.updateTeamNameByUuid(teamVo);
//        } else {
//            lockService.getLockById("team");
//            if (teamMapper.checkLeftRightCodeIsWrong() > 0) {
//                teamService.rebuildLeftRightCode();
//            }
//            String parentUuid = jsonObj.getString("parentUuid");
//            if (StringUtils.isBlank(parentUuid)) {
//                parentUuid = TeamVo.ROOT_UUID;
//            }
//            TeamVo parentTeam;
//            if (TeamVo.ROOT_UUID.equals(parentUuid)) {
//                parentTeam = teamService.buildRootTeam();
//            } else {
//                parentTeam = teamMapper.getTeamByUuid(parentUuid);
//                if (parentTeam == null) {
//                    throw new TeamNotFoundException(parentUuid);
//                }
//            }
//
//            teamVo.setParentUuid(parentUuid);
//            teamVo.setLft(parentTeam.getRht());
//            teamVo.setRht(teamVo.getLft() + 1);
//            // 更新插入位置右边的左右编码值
//            teamMapper.batchUpdateTeamLeftCode(teamVo.getLft(), 2);
//            teamMapper.batchUpdateTeamRightCode(teamVo.getLft(), 2);
//
//            teamMapper.insertTeam(teamVo);
//            List<String> userUuidList = JSON.parseArray(jsonObj.getString("userUuidList"), String.class);
//            List<String> teamUuidList = JSON.parseArray(jsonObj.getString("teamUuidList"), String.class);
//            Set<String> uuidList = userService.getUserUuidSetByUserUuidListAndTeamUuidList(userUuidList, teamUuidList);
//            if (CollectionUtils.isNotEmpty(uuidList)) {
//                for (String userUuid : uuidList) {
//                    teamMapper.insertTeamUser(new TeamUserVo(teamVo.getUuid(), userUuid));
//                }
//            }
//        }
//
//        returnObj.put("uuid", teamVo.getUuid());
//        return returnObj;
//    }
}
