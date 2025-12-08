package neatlogic.module.tenant.api.team;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.TEAM_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.TeamLevel;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.TeamUserTitleVo;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserTitleVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.team.TeamNotFoundException;
import neatlogic.framework.exception.team.UpdateTeamFoundMultiException;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AuthAction(action = TEAM_MODIFY.class)

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class TeamUpdateApi extends PrivateApiComponentBase {

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private UserMapper userMapper;


    @Override
    public String getToken() {
        return "team/update";
    }

    @Override
    public String getName() {
        return "更新分组信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    public JSONObject example() {
        JSONObject defaultJson = new JSONObject(true);
        defaultJson.put("uuid", "组uuid，不更新，只是用于查找。uuid和name入参两者必须有一个有值，如果都有值则按uuid查找");
        defaultJson.put("name", "组名，不更新，只是用于查找。uuid和name入参两者必须有一个有值，如果都有值则按uuid查找");
        defaultJson.put("email", "分组邮箱");
        defaultJson.put("phone", "分组电话");
        defaultJson.put("level", "层级");
        defaultJson.put("leaderList", new JSONArray() {{
            this.add(new JSONObject() {{
                this.put("title", "岗位名称");
                this.put("userIdList", new JSONArray() {{
                    this.add("用户id");
                }});
            }});
        }});
        return defaultJson;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "common.uuid"),
            @Param(name = "name", type = ApiParamType.STRING, desc = "common.name"),
            @Param(name = "email", type = ApiParamType.STRING, desc = "common.email", maxLength = 100),
            @Param(name = "phone", type = ApiParamType.STRING, desc = "common.phone", maxLength = 20),
            @Param(name = "level", type = ApiParamType.ENUM, member = TeamLevel.class, desc = "common.level"),
            @Param(name = "leaderList", type = ApiParamType.JSONARRAY, desc = "分组领导信息")})
    @Output({@Param(name = "uuid", type = ApiParamType.STRING, desc = "common.uuid")})
    @Description(desc = "更新分组信息，仅更新提供值的属性，不提供值的属性不更新。如果成功更新，则返回被更新分组uuid，如果没更新，则返回空值")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String level = jsonObj.getString("level");
        String email = jsonObj.getString("email");
        String phone = jsonObj.getString("phone");
        String name = jsonObj.getString("name");
        String uuid = jsonObj.getString("uuid");
        JSONArray leaderList = jsonObj.getJSONArray("leaderList");
        TeamVo teamOrigin;

        if (StringUtils.isNotBlank(uuid)) {
            teamOrigin = teamMapper.getTeamByUuid(uuid);
            if (teamOrigin == null) {
                throw new TeamNotFoundException(uuid);
            }
        } else if (StringUtils.isNotBlank(name)) {
            List<TeamVo> teamList = teamMapper.getTeamWithoutDeletedByNameList(Collections.singletonList(name));
            if (CollectionUtils.isEmpty(teamList)) {
                throw new TeamNotFoundException(name);
            }
            if (teamList.size() > 1) {
                throw new UpdateTeamFoundMultiException(name);
            }
            teamOrigin = teamList.get(0);
        } else {
            throw new ParamIrregularException("name or uuid");
        }


        TeamVo teamVo = new TeamVo();
        teamVo.setUuid(teamOrigin.getUuid());
        teamVo.setName(teamOrigin.getName());
        teamVo.setLevel(level);
        teamVo.setEmail(email);
        teamVo.setPhone(phone);
        teamMapper.updateTeamOptionalByUuid(teamVo);

        if (CollectionUtils.isNotEmpty(leaderList)) {
            Map<Long, Integer> teamUserTitleSortMap = new HashMap<>();
            List<TeamUserTitleVo> teamUserTitleVoList = teamMapper.getTeamUserTitleListByTeamUuid(teamOrigin.getUuid());
            // 顺便计算出新 title 的 sort
            Integer maxTitleSort = null;
            if (CollectionUtils.isNotEmpty(teamUserTitleVoList)) {
                for (TeamUserTitleVo teamUserTitleVo : teamUserTitleVoList) {
                    if (maxTitleSort == null || teamUserTitleVo.getTitleSort() > maxTitleSort) {
                        maxTitleSort = teamUserTitleVo.getTitleSort();
                    }
                    teamUserTitleSortMap.put(teamUserTitleVo.getTitleId(), teamUserTitleVo.getTitleSort());
                }
            }
            Integer newTitleSort = maxTitleSort;
            for (int i = 0; i < leaderList.size(); i++) {
                JSONObject leader = leaderList.getJSONObject(i);
                String title = leader.getString("title");
                List<String> userIdList = leader.getJSONArray("userIdList").toJavaList(String.class);
                if (StringUtils.isNotBlank(title)) {
                    UserTitleVo titleVo = userMapper.getUserTitleByName(title);
                    if (titleVo != null) {
                        if (newTitleSort != null) {
                            if (teamUserTitleSortMap.containsKey(titleVo.getId())) {
                                newTitleSort = teamUserTitleSortMap.get(titleVo.getId());
                            } else {
                                newTitleSort++;
                            }
                        } else {
                            newTitleSort = 0;
                        }
                        if (CollectionUtils.isNotEmpty(userIdList)) {
                            teamMapper.deleteTeamUserTitleByTeamUuidAndTitleId(teamVo.getUuid(), titleVo.getId());
                            for (String userId : userIdList) {
                                UserVo userVo = userMapper.getUserByUserId(userId);
                                if (userVo != null) {
                                    teamMapper.insertTeamUserTitle(teamVo.getUuid(), userVo.getUuid(), titleVo.getId(), newTitleSort);
                                }
                            }
                        }
                    }
                }
            }
        }

        return teamVo.getUuid();
    }


}
