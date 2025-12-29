/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.user;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.auth.core.AuthBase;
import neatlogic.framework.auth.core.AuthFactory;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.dto.UserAuthVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.dto.module.ModuleGroupVo;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.service.AuthenticationInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@NoPasswordExpiredCheck
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class CurrentUserGetApi extends PrivateApiComponentBase {

    @Resource
    private AuthenticationInfoService authenticationInfoService;

    @Resource
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "/user/current/get";
    }

    @Override
    public String getName() {
        return "获取当前用户信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Output({})
    @Description(desc = "获取当前用户信息")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        UserContext userContext = UserContext.get();
        if (userContext != null) {
            JSONObject userObj = new JSONObject();
            UserVo userVo = userMapper.getUserBaseInfoByUuid(userContext.getUserUuid());
            if (userVo == null) {
                throw new UserNotFoundException(userContext.getUserUuid());
            }
            AuthenticationInfoVo authenticationInfoVo = userContext.getAuthenticationInfoVo();
            if (authenticationInfoVo == null) {
                authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(userContext.getUserUuid(), true);
            }
            //超级管理员拥有所有权限
            List<UserAuthVo> userAuthList = new ArrayList<>();
            if (userVo.getIsSuperAdmin() != null && userVo.getIsSuperAdmin()) {
                List<AuthBase> authBaseList = AuthFactory.getAuthList();
                for (AuthBase authBase : authBaseList) {
                    String authGroupName = authBase.getAuthGroup();
                    if (!TenantContext.get().getActiveModuleMap().containsKey(authGroupName)) {
                        continue;
                    }
                    userAuthList.add(new UserAuthVo(userContext.getUserUuid(), authBase));
                }
            } else {
                List<UserAuthVo> userAuthVoList = userMapper.searchUserAllAuthByUserAuth(authenticationInfoVo);
                if (CollectionUtils.isNotEmpty(userAuthVoList)) {
                    userAuthVoList.forEach(auth -> {
                        //过滤反射后不存在非法auth
                        AuthBase authBase = AuthFactory.getAuthInstance(auth.getAuth());
                        if (authBase != null) {
                            List<ModuleGroupVo> moduleGroupVos = TenantContext.get().getActiveModuleGroupList();
                            List<String> activeModuleGroupList = moduleGroupVos.stream().map(ModuleGroupVo::getGroup).collect(Collectors.toList());
                            //过滤该租户没有tenantGroup对应的auth
                            if (CollectionUtils.isNotEmpty(moduleGroupVos) && activeModuleGroupList.contains(auth.getAuthGroup())) {
                                userAuthList.add(auth);
                            }
                        }
                    });

                    AuthActionChecker.getAuthList(userAuthList);
                }
            }
            userObj.put("uuid", userContext.getUserUuid());
            userObj.put("userId", userContext.getUserId());
            userObj.put("userName", userContext.getUserName());
            List<String> newTeamUuidList = new ArrayList<>();
            List<String> teamUuidList = authenticationInfoVo.getTeamUuidList();
            if (CollectionUtils.isNotEmpty(teamUuidList)) {
                for (String teamUuid : teamUuidList) {
                    newTeamUuidList.add(GroupSearch.TEAM.addPrefix(teamUuid));
                }
            }
            List<String> newRoleUuidList = new ArrayList<>();
            List<String> roleUuidList = authenticationInfoVo.getRoleUuidList();
            if (CollectionUtils.isNotEmpty(roleUuidList)) {
                for (String roleUuid : roleUuidList) {
                    newRoleUuidList.add(GroupSearch.ROLE.addPrefix(roleUuid));
                }
            }
            userObj.put("teamUuidList", newTeamUuidList);
            userObj.put("roleUuidList", newRoleUuidList);
            userObj.put("userAuthList", userAuthList);
            JSONObject userInfoObj = userVo.getUserInfoObj();
            if (MapUtils.isNotEmpty(userInfoObj)) {
                String avatar = userInfoObj.getString("avatar");
                userObj.put("avatar", avatar);
            }
            return userObj;
        }
        return null;
    }
}
