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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.auth.core.AuthFactory;
import neatlogic.framework.auth.core.AuthBase;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.RoleAuthVo;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserAuthVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 分别查询页面、角色和代码来源权限，复用统一权限包含关系。 */
@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class UserAuthSearchApi extends PrivateApiComponentBase {
    
    @Resource
    private UserMapper userMapper;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private RoleMapper roleMapper;

    @Override
    public String getToken() {
        return "user/auth/search";
    }

    @Override
    public String getName() {
        return "auth.user.search";
    }

    @Override
    public String getConfig() {
        return null;
    }

    /**
     * 页面直接授权与代码内置授权分别返回，内置权限及其包含权限只读展示。
     */
    @Input({
            @Param( name = "userUuid", type = ApiParamType.STRING, desc = "common.useruuid", isRequired = true)
    })
    @Output({
            @Param( name = "userAuthObj", type = ApiParamType.JSONOBJECT, desc = "auth.user.pageauth"),
            @Param( name = "userRoleAuthObj", type = ApiParamType.JSONOBJECT, desc = "auth.user.roleauth"),
            @Param(name = "userCodeAuthObj", type = ApiParamType.JSONOBJECT, desc = "auth.systemuser.codeauth")
    })
    @Description(desc = "auth.user.search")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        String userUuid = jsonObj.getString("userUuid");
        List<UserAuthVo> userAuthList = userMapper.searchUserAuthByUserUuid(userUuid);
        List<RoleAuthVo> userRoleAuthList = userMapper.searchUserRoleAuthByUserUuid(userUuid);

        Map<String, String> userRoleAuthMap = new HashMap<>();

        JSONObject userRoleAuthObj = new JSONObject();

        if (CollectionUtils.isNotEmpty(userRoleAuthList)) {
            for (RoleAuthVo roleAuth : userRoleAuthList) {
                if (userRoleAuthMap.containsKey(roleAuth.getAuth())){
                    if (roleAuth.getAuthGroup().equals(userRoleAuthMap.get(roleAuth.getAuth()))){
                        continue;
                    }
                }
                userRoleAuthMap.put(roleAuth.getAuth(), roleAuth.getAuthGroup());
                if (userRoleAuthObj.containsKey(roleAuth.getAuthGroup())){
                    JSONArray authArray = userRoleAuthObj.getJSONArray(roleAuth.getAuthGroup());
                    authArray.add(roleAuth.getAuth());
                }else {
                    JSONArray authArray = new JSONArray();
                    authArray.add(roleAuth.getAuth());
                    userRoleAuthObj.put(roleAuth.getAuthGroup(), authArray);
                }
            }
        }

        List<String> teamUuidList = new ArrayList<>();
        List<RoleVo> teamRoleList = new ArrayList<>();
        List<TeamVo> teamList = teamMapper.getTeamListByUserUuid(userUuid);
        for (TeamVo teamVo : teamList) {
            teamUuidList.add(teamVo.getUuid());
            List<RoleVo> list = roleMapper.getParentTeamRoleListWithCheckedChildrenByTeam(teamVo);
            teamRoleList.addAll(list);
        }
        if (CollectionUtils.isNotEmpty(teamUuidList)) {
            List<RoleVo> list = roleMapper.getRoleListWithTeamByTeamUuidList(teamUuidList);
            teamRoleList.addAll(list);
        }
        if (CollectionUtils.isNotEmpty(teamRoleList)) {
            List<String> roleUuidList = teamRoleList.stream().map(RoleVo::getUuid).collect(Collectors.toList());
            List<RoleAuthVo> teamRoleAuthList = roleMapper.searchRoleAuthByRoleUuidList(roleUuidList);
            if (CollectionUtils.isNotEmpty(teamRoleAuthList)) {
                for (RoleAuthVo roleAuth : teamRoleAuthList) {
                    if (userRoleAuthMap.containsKey(roleAuth.getAuth())){
                        if (roleAuth.getAuthGroup().equals(userRoleAuthMap.get(roleAuth.getAuth()))){
                            continue;
                        }
                    }
                    userRoleAuthMap.put(roleAuth.getAuth(), roleAuth.getAuthGroup());
                    if (userRoleAuthObj.containsKey(roleAuth.getAuthGroup())){
                        JSONArray authArray = userRoleAuthObj.getJSONArray(roleAuth.getAuthGroup());
                        authArray.add(roleAuth.getAuth());
                    }else {
                        JSONArray authArray = new JSONArray();
                        authArray.add(roleAuth.getAuth());
                        userRoleAuthObj.put(roleAuth.getAuthGroup(), authArray);
                    }
                }
            }
        }
        JSONObject userAuthObj = new JSONObject();
        if (CollectionUtils.isNotEmpty(userAuthList)) {
            for (UserAuthVo authVo : userAuthList) {
                boolean sameAuth = userRoleAuthMap.containsKey(authVo.getAuth());
                boolean sameGroup = false;
                if (sameAuth){
                    sameGroup = userRoleAuthMap.get(authVo.getAuth()).equals(authVo.getAuthGroup());
                }
                if (!sameAuth || !sameGroup) {
                    if (userAuthObj.containsKey(authVo.getAuthGroup())){
                        JSONArray authArray = userAuthObj.getJSONArray(authVo.getAuthGroup());
                        authArray.add(authVo.getAuth());
                    }else {
                        JSONArray authArray = new JSONArray();
                        authArray.add(authVo.getAuth());
                        userAuthObj.put(authVo.getAuthGroup(), authArray);
                    }
                }
            }
        }
        returnObj.put("userAuthObj", userAuthObj);
        returnObj.put("userRoleAuthObj", userRoleAuthObj);
        List<UserAuthVo> codeAuthList = new ArrayList<>();
        for (AuthBase auth : AuthFactory.getDefaultAuthListBySystemUser(userUuid)) {
            codeAuthList.add(new UserAuthVo(userUuid, auth));
        }
        AuthActionChecker.getAuthList(codeAuthList);
        JSONObject userCodeAuthObj = new JSONObject();
        for (UserAuthVo auth : codeAuthList) {
            userCodeAuthObj.computeIfAbsent(auth.getAuthGroup(), key -> new JSONArray());
            userCodeAuthObj.getJSONArray(auth.getAuthGroup()).add(auth.getAuth());
        }
        returnObj.put("userCodeAuthObj", userCodeAuthObj);
        return returnObj;
    }
}
