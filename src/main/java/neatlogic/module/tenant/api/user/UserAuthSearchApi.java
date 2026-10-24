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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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
            @Param(name = "userCodeAuthObj", type = ApiParamType.JSONOBJECT, desc = "auth.systemuser.codeauth"),
            // 只新增来源说明，不改变既有授权字段及其过滤规则。
            @Param(name = "userRoleAuthSourcePathList", type = ApiParamType.JSONARRAY, desc = "角色权限来源路径列表")
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
        // 独立记录角色对应的用户所属分组，避免权限去重或祖先绑定点丢失来源。
        Map<String, Set<String>> roleTeamUuidMap = new HashMap<>();
        List<RoleAuthVo> teamRoleAuthList = Collections.emptyList();
        List<TeamVo> teamList = teamMapper.getTeamListByUserUuid(userUuid);
        for (TeamVo teamVo : teamList) {
            teamUuidList.add(teamVo.getUuid());
            List<RoleVo> list = roleMapper.getParentTeamRoleListWithCheckedChildrenByTeam(teamVo);
            teamRoleList.addAll(list);
            // 祖先角色继承始终展示当前用户所属分组的完整路径。
            for (RoleVo role : list) {
                roleTeamUuidMap.computeIfAbsent(role.getUuid(), key -> new LinkedHashSet<>()).add(teamVo.getUuid());
            }
        }
        if (CollectionUtils.isNotEmpty(teamUuidList)) {
            List<RoleVo> list = roleMapper.getRoleListWithTeamByTeamUuidList(teamUuidList);
            teamRoleList.addAll(list);
            // 一次查询可能返回同一角色的多个分组绑定，逐个保留关联。
            for (RoleVo role : list) {
                for (TeamVo team : CollectionUtils.emptyIfNull(role.getTeamList())) {
                    roleTeamUuidMap.computeIfAbsent(role.getUuid(), key -> new LinkedHashSet<>()).add(team.getUuid());
                }
            }
        }
        if (CollectionUtils.isNotEmpty(teamRoleList)) {
            List<String> roleUuidList = teamRoleList.stream().map(RoleVo::getUuid).collect(Collectors.toList());
            teamRoleAuthList = roleMapper.searchRoleAuthByRoleUuidList(roleUuidList);
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
        // 来源组装独立于旧逻辑，在权限去重后仍保留每个角色、每个所属分组的来源。
        returnObj.put("userRoleAuthSourcePathList", getRoleAuthSourcePathList(userRoleAuthList, teamRoleAuthList, roleTeamUuidMap));
        return returnObj;
    }

    /** 批量补充角色及分组名称，直接角色优先，同类来源按文字排序并去重。 */
    private JSONArray getRoleAuthSourcePathList(List<RoleAuthVo> directAuthList, List<RoleAuthVo> teamAuthList,
                                               Map<String, Set<String>> roleTeamUuidMap) {
        Set<String> roleUuidSet = new LinkedHashSet<>();
        for (RoleAuthVo auth : CollectionUtils.emptyIfNull(directAuthList)) {
            if (StringUtils.isNotBlank(auth.getRoleUuid())) {
                roleUuidSet.add(auth.getRoleUuid());
            }
        }
        roleUuidSet.addAll(roleTeamUuidMap.keySet());
        Map<String, String> roleNameMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(roleUuidSet)) {
            for (RoleVo role : roleMapper.getRoleByUuidList(new ArrayList<>(roleUuidSet))) {
                if (StringUtils.isNotBlank(role.getName())) {
                    roleNameMap.put(role.getUuid(), role.getName().endsWith("角色") ? role.getName() : role.getName() + "角色");
                }
            }
        }
        Set<String> teamUuidSet = new LinkedHashSet<>();
        roleTeamUuidMap.values().forEach(teamUuidSet::addAll);
        Map<String, String> teamPathMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(teamUuidSet)) {
            for (TeamVo team : teamMapper.getTeamByUuidList(new ArrayList<>(teamUuidSet))) {
                // 本次请求每个分组只解析一次，复用已保存路径；缺失时再查祖先及自身。
                String path = team.getUpwardNamePath();
                if (StringUtils.isBlank(path)) {
                    path = getTeamSourcePath(team);
                }
                if (StringUtils.isNotBlank(path)) {
                    teamPathMap.put(team.getUuid(), path.endsWith("分组") ? path : path + "分组");
                }
            }
        }
        Map<String, Map<String, Set<String>>> directSources = new TreeMap<>();
        Map<String, Map<String, Set<String>>> teamSources = new TreeMap<>();
        for (RoleAuthVo auth : CollectionUtils.emptyIfNull(directAuthList)) {
            addSourcePath(directSources, auth, roleNameMap.get(auth.getRoleUuid()));
        }
        for (RoleAuthVo auth : CollectionUtils.emptyIfNull(teamAuthList)) {
            String roleName = roleNameMap.get(auth.getRoleUuid());
            if (roleName == null) {
                continue;
            }
            for (String teamUuid : roleTeamUuidMap.getOrDefault(auth.getRoleUuid(), Collections.emptySet())) {
                String teamPath = teamPathMap.get(teamUuid);
                if (teamPath != null) {
                    addSourcePath(teamSources, auth, teamPath + "的" + roleName);
                }
            }
        }
        // 使用嵌套映射按权限组、编码匹配，避免同编码跨组串用来源。
        Map<String, Map<String, Set<String>>> sources = new TreeMap<>();
        for (Map<String, Map<String, Set<String>>> category : java.util.Arrays.asList(directSources, teamSources)) {
            category.forEach((group, authMap) -> authMap.forEach((auth, paths) -> sources
                    .computeIfAbsent(group, key -> new TreeMap<>())
                    .computeIfAbsent(auth, key -> new LinkedHashSet<>()).addAll(paths)));
        }
        JSONArray result = new JSONArray();
        sources.forEach((group, authMap) -> authMap.forEach((auth, paths) -> {
            JSONObject source = new JSONObject();
            source.put("authGroup", group);
            source.put("auth", auth);
            source.put("sourcePathList", new ArrayList<>(paths));
            result.add(source);
        }));
        return result;
    }

    /** 缺失路径时按祖先层级重建；资料不完整时留空，由前端回退原只读提示。 */
    private String getTeamSourcePath(TeamVo team) {
        if (team.getLft() == null || team.getRht() == null) {
            return null;
        }
        List<TeamVo> ancestors = teamMapper.getAncestorsAndSelfByLftRht(team.getLft(), team.getRht(), null);
        if (CollectionUtils.isEmpty(ancestors)) {
            return null;
        }
        // 首节点也必须连接到根，避免缺失最上层祖先时把局部路径当作完整路径。
        String parentUuid = ancestors.get(0).getParentUuid();
        if (!TeamVo.ROOT_UUID.equals(parentUuid) && !TeamVo.ROOT_PARENTUUID.equals(parentUuid)) {
            return null;
        }
        List<String> names = new ArrayList<>();
        TeamVo previous = null;
        for (TeamVo ancestor : ancestors) {
            if (StringUtils.isBlank(ancestor.getName()) || (previous != null
                    && !previous.getUuid().equals(ancestor.getParentUuid()))) {
                return null;
            }
            names.add(ancestor.getName());
            previous = ancestor;
        }
        return team.getUuid().equals(previous.getUuid()) ? String.join("/", names) : null;
    }

    /** 同一权限内合并来源，使用有序集合保证多角色重复授权只显示一条相同路径。 */
    private void addSourcePath(Map<String, Map<String, Set<String>>> sources, RoleAuthVo auth, String path) {
        if (StringUtils.isBlank(path) || StringUtils.isBlank(auth.getAuthGroup()) || StringUtils.isBlank(auth.getAuth())) {
            return;
        }
        sources.computeIfAbsent(auth.getAuthGroup(), key -> new TreeMap<>())
                .computeIfAbsent(auth.getAuth(), key -> new TreeSet<>()).add(path);
    }
}
