/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.user;

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
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserAuthVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

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
        return "????????????id????????????????????????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "userUuid", type = ApiParamType.STRING, desc = "??????uuid", isRequired = false)})
    @Output({@Param(name = "Return", explode = UserVo.class, desc = "????????????")})
    @Description(desc = "????????????Id??????????????????")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String userUuid = jsonObj.getString("userUuid");
        if (StringUtils.isBlank(userUuid)) {
            userUuid = UserContext.get().getUserUuid(true);
        }
        UserVo userVo = null;
        //??????????????? ??????????????????????????????
        if (Config.ENABLE_SUPERADMIN() && Config.SUPERADMIN().equals(UserContext.get().getUserId())) {
            userVo = MaintenanceMode.getMaintenanceUser();
            //?????????????????????????????????
            userVo.setIsMaintenanceMode(1);
        } else if (Objects.equals(SystemUser.SYSTEM.getUserUuid(), userUuid)) {
            userVo = SystemUser.SYSTEM.getUserVo();
        } else {
            userVo = userMapper.getUserByUuid(userUuid);
            if (userVo == null) {
                throw new UserNotFoundException(userUuid);
            }
            //?????????????????????????????????
            if (userVo.getIsSuperAdmin() != null && userVo.getIsSuperAdmin()) {
                List<AuthBase> authBaseList = AuthFactory.getAuthList();
                List<UserAuthVo> userAuthVos = new ArrayList<>();
                for (AuthBase authBase : authBaseList) {
                    userAuthVos.add(new UserAuthVo(userUuid, authBase));
                }
                userVo.setUserAuthList(userAuthVos);
            } else {
                List<UserAuthVo> userAuthVoList = userMapper.searchUserAllAuthByUserAuth(new UserAuthVo(userUuid));
                if (CollectionUtils.isNotEmpty(userAuthVoList)) {
                    AuthActionChecker.getAuthList(userAuthVoList);
                    userVo.setUserAuthList(userAuthVoList);
                }
            }
            List<TeamVo> teamList = teamMapper.getTeamListByUserUuid(userUuid);
            List<String> teamUuidList = userVo.getTeamUuidList();
            /**
             * ????????????????????????,????????????a????????????
             * 1?????????a??????????????????????????????????????????roleList
             * 2?????????a????????????roleList
             * 3???????????????????????????roleList ???role???uuid??????????????????
             */
            List<RoleVo> teamRoleList = new ArrayList<>();
            Map<String, RoleVo> roleVoMap = new HashMap<>();
            teamList.forEach(e -> teamRoleList.addAll(roleMapper.getParentTeamRoleListWithCheckedChildrenByTeam(e)));
            if (CollectionUtils.isNotEmpty(teamUuidList)) {
                teamRoleList.addAll(roleMapper.getRoleListByTeamUuidList(teamUuidList));
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
//		JSONObject userJson = (JSONObject) JSON.toJSON(userVo);// ????????????cache vo
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
        //?????????????????????????????????
//		userJson.put("isMaintenanceMode", 0);
//		if (Config.ENABLE_SUPERADMIN()) {
//			userJson.put("isMaintenanceMode", 1);
//		}
//		return userJson;
    }
}
