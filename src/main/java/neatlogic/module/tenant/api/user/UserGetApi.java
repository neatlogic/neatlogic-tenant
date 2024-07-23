/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.user;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.auth.core.AuthBase;
import neatlogic.framework.auth.core.AuthFactory;
import neatlogic.framework.auth.init.MaintenanceMode;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.common.constvalue.SystemUser;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.*;
import neatlogic.framework.dto.module.ModuleGroupVo;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.service.AuthenticationInfoService;
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

    @Resource
    private AuthenticationInfoService authenticationInfoService;

    @Override
    public String getToken() {
        return "user/get";
    }

    @Override
    public String getName() {
        return "nmtau.usergetapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "userUuid", type = ApiParamType.STRING, desc = "common.useruuid"),
            @Param(name = "isRuleRole", type = ApiParamType.BOOLEAN, desc = "nmtau.usergetapi.input.param.desc.isrulerole")
    })
    @Output({@Param(name = "Return", explode = UserVo.class, desc = "nmtau.usergetapi.output.param.desc.user")})
    @Description(desc = "nmtau.usergetapi.description.desc")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String userUuid = jsonObj.getString("userUuid");
        Boolean isRuleRole = true;
        AuthenticationInfoVo authenticationInfoVo = null;
        if (jsonObj.containsKey("isRuleRole")) {
            isRuleRole = jsonObj.getBoolean("isRuleRole");
        }
        UserVo userVo;
        //维护模式下 获取厂商维护人员信息
        if (Config.ENABLE_MAINTENANCE() && Config.MAINTENANCE().equals(UserContext.get().getUserId()) && StringUtils.isBlank(jsonObj.getString("userUuid"))) {
            userVo = MaintenanceMode.getMaintenanceUser();
            //告诉前端是否为维护模式
            userVo.setIsMaintenanceMode(1);
        } else if (Objects.equals(SystemUser.SYSTEM.getUserUuid(), userUuid)) {
            userVo = SystemUser.SYSTEM.getUserVo(false);
        } else {
            if (StringUtils.isBlank(userUuid)) {
                userUuid = UserContext.get().getUserUuid(true);
                authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
            } else {
                authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(userUuid, isRuleRole);
            }
            userVo = userMapper.getUserByUuid(userUuid);
            if (userVo == null) {
                throw new UserNotFoundException(userUuid);
            }
            userVo.setTeamUuidList(authenticationInfoVo.getTeamUuidList());
//            userVo.setRoleUuidList(authenticationInfoVo.getRoleUuidList());
            userVo.setRoleUuidList(roleMapper.getRoleUuidListByUserUuid(userUuid));
            if (CollectionUtils.isNotEmpty(userVo.getRoleUuidList())) {
                userVo.setRoleList(roleMapper.getRoleByUuidList(userVo.getRoleUuidList()));
            }
            //超级管理员拥有所有权限
            if (userVo.getIsSuperAdmin() != null && userVo.getIsSuperAdmin()) {
                List<AuthBase> authBaseList = AuthFactory.getAuthList();
                List<UserAuthVo> userAuthVos = new ArrayList<>();
                for (AuthBase authBase : authBaseList) {
                    String authGroupName = authBase.getAuthGroup();
                    if (!TenantContext.get().getActiveModuleMap().containsKey(authGroupName)) {
                        continue;
                    }
                    userAuthVos.add(new UserAuthVo(userUuid, authBase));
                }
                userVo.setUserAuthList(userAuthVos);
            } else {

                List<UserAuthVo> userAuthVoList = userMapper.searchUserAllAuthByUserAuth(authenticationInfoVo);
                List<UserAuthVo> filteredUserAuthVoList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(userAuthVoList)) {
                    userAuthVoList.forEach(auth -> {
                        //过滤反射后不存在非法auth
                        AuthBase authBase = AuthFactory.getAuthInstance(auth.getAuth());
                        if (authBase != null) {
                            List<ModuleGroupVo> moduleGroupVos = TenantContext.get().getActiveModuleGroupList();
                            List<String> activeModuleGroupList = moduleGroupVos.stream().map(ModuleGroupVo::getGroup).collect(Collectors.toList());
                            //过滤该租户没有tenantGroup对应的auth
                            if (CollectionUtils.isNotEmpty(moduleGroupVos) && activeModuleGroupList.contains(auth.getAuthGroup())) {
                                filteredUserAuthVoList.add(auth);
                            }
                        }
                    });

                    AuthActionChecker.getAuthList(filteredUserAuthVoList);
                    userVo.setUserAuthList(filteredUserAuthVoList);
                }
            }
            List<TeamVo> teamList = teamMapper.getTeamListByUserUuid(userUuid);
            userVo.setTeamList(teamList);
            List<String> teamUuidList = userVo.getTeamUuidList();
            /**
             * 补充分组角色信息,以用户的a分组为例
             * 1、根据a分组的父节点（需要穿透）找到roleList
             * 2、根据a分组找到roleList
             * 3、将以上两点找到的roleList 以role的uuid为唯一键合并
             */
            List<RoleVo> teamRoleList = new ArrayList<>();
            Map<String, RoleVo> roleVoMap = new HashMap<>();
            teamList.forEach(e -> teamRoleList.addAll(roleMapper.getParentTeamRoleListWithCheckedChildrenByTeam(e)));
            if (CollectionUtils.isNotEmpty(teamUuidList)) {
                teamRoleList.addAll(roleMapper.getRoleListWithTeamByTeamUuidList(teamUuidList));
            }
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
    }
}
