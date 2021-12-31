/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.user;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.auth.init.MaintenanceMode;
import codedriver.framework.common.config.Config;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.constvalue.SystemUser;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class UserGetApi extends PrivateApiComponentBase {

    @Resource
    private UserMapper userMapper;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private RoleMapper roleMapper;

    @Override
    public String getToken() {
        return "user/get";
    }

    @Override
    public String getName() {
        return "根据用户id查询用户详情接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "userUuid", type = ApiParamType.STRING, desc = "用户uuid", isRequired = false)})
    @Output({@Param(name = "Return", explode = UserVo.class, desc = "用户详情")})
    @Description(desc = "根据用户Id查询用户详情")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String userUuid = jsonObj.getString("userUuid");
        if (StringUtils.isBlank(userUuid)) {
            userUuid = UserContext.get().getUserUuid(true);
        }
        UserVo userVo = null;
        //维护模式下 获取厂商维护人员信息
        if (Config.ENABLE_SUPERADMIN() && MaintenanceMode.MAINTENANCE_USER.equals(UserContext.get().getUserId())) {
            userVo = MaintenanceMode.getMaintenanceUser();
            //告诉前端是否为维护模式
            userVo.setIsMaintenanceMode(1);
        } else if (Objects.equals(SystemUser.SYSTEM.getUserUuid(), userUuid)) {
            userVo = SystemUser.SYSTEM.getUserVo();
        } else {
            userVo = userMapper.getUserByUuid(userUuid);
            if (userVo == null) {
                throw new UserNotFoundException(userUuid);
            }
            List<UserAuthVo> userAuthVoList = userMapper.searchUserAllAuthByUserAuth(new UserAuthVo(userUuid));
            if (CollectionUtils.isNotEmpty(userAuthVoList)) {
                AuthActionChecker.getAuthList(userAuthVoList);
                userVo.setUserAuthList(userAuthVoList);
            }
            List<TeamVo> teamList = teamMapper.getTeamListByUserUuid(userUuid);
            List<String> teamUuidList = teamList.stream().map(TeamVo::getUuid).collect(Collectors.toList());
            /**
             * 补充分组角色信息,以用户的a分组为例
             * 1、根据a分组的父节点（需要穿透）找到parentTeamRoleList
             * 2、根据a分组找到ownTeamRoleList
             * 3、将以上两点找到的以role的uuid为唯一键合并
             */
            List<RoleVo> teamRoleList = new ArrayList<>();
            Map<String, RoleVo> roleVoMap = new HashMap<>();
            teamList.forEach(e -> teamRoleList.addAll(roleMapper.getParentTeamRoleListWithCheckedChildrenByTeam(e)));
            teamRoleList.addAll( roleMapper.getRoleListByTeamUuidList(teamUuidList));
            for (RoleVo roleVo : teamRoleList) {
                String uuid = roleVo.getUuid();
                if (!roleVoMap.containsKey(uuid)) {
                    roleVoMap.put(uuid, roleVo);
                } else {
                    roleVoMap.get(uuid).getTeamList().addAll(roleVo.getTeamList());
                }
            }

            userVo.setTeamRoleList(new ArrayList<>(roleVoMap.values()));

            if (CollectionUtils.isNotEmpty(teamUuidList)) {
                for (int i = 0; i < teamUuidList.size(); i++) {
                    String teamUuid = teamUuidList.get(i);
                    teamUuid = GroupSearch.TEAM.getValuePlugin() + teamUuid;
                    teamUuidList.set(i, teamUuid);
                }
            }
            List<String> roleUuidList = userVo.getRoleUuidList();
            if (CollectionUtils.isNotEmpty(roleUuidList)) {
                for (int i = 0; i < roleUuidList.size(); i++) {
                    String roleUuid = roleUuidList.get(i);
                    roleUuid = GroupSearch.ROLE.getValuePlugin() + roleUuid;
                    roleUuidList.set(i, roleUuid);

                }
            }
        }
        return userVo;
//		JSONObject userJson = (JSONObject) JSON.toJSON(userVo);// 防止修改cache vo
//		if (CollectionUtils.isNotEmpty(userJson.getJSONArray("teamUuidList"))) {
//			List<String> teamUuidList = new ArrayList<>();
//			for (Object teamUuid : userJson.getJSONArray("teamUuidList")) {
//				teamUuidList.add(GroupSearch.TEAM.getValuePlugin() + teamUuid);
//			}
//			userJson.put("teamUuidList", teamUuidList);
//		}
//		if (CollectionUtils.isNotEmpty(userJson.getJSONArray("roleUuidList"))) {
//			List<String> roleUuidList = new ArrayList<>();
//			for (Object roleUuid : userJson.getJSONArray("roleUuidList")) {
//				roleUuidList.add(GroupSearch.ROLE.getValuePlugin() + roleUuid);
//			}
//			userJson.put("roleUuidList", roleUuidList);
//		}
        //告诉前端是否为维护模式
//		userJson.put("isMaintenanceMode", 0);
//		if (Config.ENABLE_SUPERADMIN()) {
//			userJson.put("isMaintenanceMode", 1);
//		}
//		return userJson;
    }
}
