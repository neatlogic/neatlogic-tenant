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
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
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
import neatlogic.framework.restful.constvalue.ApiAnonymousAccessSupportEnum;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Queue;
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

    /**
     * 是否支持匿名访问
     *
     * @return true false
     */
    @Override
    public ApiAnonymousAccessSupportEnum supportAnonymousAccess() {
        return super.supportAnonymousAccess();
    }

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
     * 是否禁用返回值循环引用检查
     *
     * @return true false
     */
    @Override
    public boolean disableReturnCircularReferenceDetect() {
        return true;
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
            @Param(name = "userRoleAuthSourcePathList", type = ApiParamType.JSONARRAY, desc = "nmtau.userauthsearchapi.output.param.desc")
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
        // 页面直接授权已经生成，随后合并包含项不会把重复的直接授权从保存集合中滤掉。
        returnObj.put("userRoleAuthSourcePathList", getRoleAuthSourcePathList(userAuthList, userRoleAuthList,
                teamRoleAuthList, roleTeamUuidMap, userRoleAuthObj));
        return returnObj;
    }

    /** 批量补充原始角色名称及结构化分组路径，展示文案由前端国际化处理。 */
    private JSONArray getRoleAuthSourcePathList(List<UserAuthVo> userAuthList, List<RoleAuthVo> directAuthList,
                                               List<RoleAuthVo> teamAuthList, Map<String, Set<String>> roleTeamUuidMap,
                                               JSONObject userRoleAuthObj) {
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
                    roleNameMap.put(role.getUuid(), role.getName());
                }
            }
        }
        Set<String> teamUuidSet = new LinkedHashSet<>();
        roleTeamUuidMap.values().forEach(teamUuidSet::addAll);
        Map<String, TeamVo> teamMap = new HashMap<>();
        Map<String, List<String>> teamPathMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(teamUuidSet)) {
            for (TeamVo team : teamMapper.getTeamByUuidList(new ArrayList<>(teamUuidSet))) {
                // 每个分组仅解析一次；原始名称不增删后缀，缺路径时复用祖先查询。
                List<String> names = StringUtils.isBlank(team.getUpwardNamePath())
                        ? getTeamSourcePath(team) : Arrays.asList(team.getUpwardNamePath().split("/", -1));
                if (CollectionUtils.isNotEmpty(names) && names.stream().noneMatch(StringUtils::isBlank)) {
                    teamMap.put(team.getUuid(), team);
                    teamPathMap.put(team.getUuid(), names);
                }
            }
        }
        // 按权限组和权限编码隔离来源，同角色在不同所属分组下的来源分别保留。
        Map<String, Map<String, List<SourcePathVo>>> sources = new TreeMap<>();
        for (RoleAuthVo auth : CollectionUtils.emptyIfNull(directAuthList)) {
            String name = roleNameMap.get(auth.getRoleUuid());
            if (name != null) {
                addSourcePath(sources, auth, new SourcePathVo(auth.getRoleUuid(), name, null, Collections.emptyList(), null));
            }
        }
        for (RoleAuthVo auth : CollectionUtils.emptyIfNull(teamAuthList)) {
            String roleName = roleNameMap.get(auth.getRoleUuid());
            if (roleName == null) {
                continue;
            }
            for (String teamUuid : roleTeamUuidMap.getOrDefault(auth.getRoleUuid(), Collections.emptySet())) {
                TeamVo team = teamMap.get(teamUuid);
                if (team != null) {
                    addSourcePath(sources, auth, new SourcePathVo(auth.getRoleUuid(), roleName,
                            teamUuid, teamPathMap.get(teamUuid), team.getLft()));
                }
            }
        }
        // 仅从数据库授权展开；代码默认授权仍完全由原来的userCodeAuthObj流程处理。
        addIncludedAuthSources(userAuthList, directAuthList, teamAuthList, sources, userRoleAuthObj);
        // 原角色来源优先；包含来源按用户直接、直接角色、分组树顺序排列，根权限编码作稳定次序。
        Comparator<SourcePathVo> comparator = Comparator.comparing((SourcePathVo source) -> source instanceof IncludeSourcePathVo)
                .thenComparing(source -> source.getRoleUuid() != null)
                .thenComparing(source -> source.getTeamUuid() != null)
                .thenComparing(SourcePathVo::getTeamLft, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(SourcePathVo::getTeamUuid, Comparator.nullsFirst(String::compareTo))
                .thenComparing(SourcePathVo::getRoleName, Comparator.nullsFirst(String::compareTo))
                .thenComparing(SourcePathVo::getRoleUuid, Comparator.nullsFirst(String::compareTo))
                .thenComparing(this::getSourceRootKey);
        JSONArray result = new JSONArray();
        sources.forEach((group, authMap) -> authMap.forEach((auth, paths) -> {
            paths.sort(comparator);
            JSONObject source = new JSONObject();
            source.put("authGroup", group);
            source.put("auth", auth);
            source.put("sourcePathList", paths);
            result.add(source);
        }));
        return result;
    }

    /** 独立展开数据库根权限；角色资料缺失也要回显包含权限，来源不足时由前端回退提示。 */
    private void addIncludedAuthSources(List<UserAuthVo> userAuthList, List<RoleAuthVo> directAuthList,
                                        List<RoleAuthVo> teamAuthList, Map<String, Map<String, List<SourcePathVo>>> sources,
                                        JSONObject userRoleAuthObj) {
        Set<String> roots = new TreeSet<>();
        Map<String, List<SourcePathVo>> rootSources = new HashMap<>();
        for (UserAuthVo auth : CollectionUtils.emptyIfNull(userAuthList)) {
            roots.add(auth.getAuth());
            // 用户直接授权没有角色和分组信息，显式null字段继续由局部序列化配置保留。
            rootSources.computeIfAbsent(auth.getAuth(), key -> new ArrayList<>())
                    .add(new SourcePathVo(null, null, null, Collections.emptyList(), null));
        }
        List<RoleAuthVo> roleAuths = new ArrayList<>(CollectionUtils.emptyIfNull(directAuthList));
        roleAuths.addAll(CollectionUtils.emptyIfNull(teamAuthList));
        for (RoleAuthVo auth : roleAuths) {
            roots.add(auth.getAuth());
            // 此时sources仅含原角色来源，快照后再展开，避免把包含项当成新的数据库根权限。
            for (SourcePathVo source : sources.getOrDefault(auth.getAuthGroup(), Collections.emptyMap())
                    .getOrDefault(auth.getAuth(), Collections.emptyList())) {
                if (Objects.equals(source.getRoleUuid(), auth.getRoleUuid())) {
                    rootSources.computeIfAbsent(auth.getAuth(), key -> new ArrayList<>()).add(source);
                }
            }
        }
        for (String rootName : roots) {
            AuthBase root = AuthFactory.getAuthInstance(rootName);
            if (root == null) {
                continue;
            }
            for (Map.Entry<AuthBase, List<JSONObject>> entry : getIncludeAuthPaths(root).entrySet()) {
                AuthBase target = entry.getKey();
                userRoleAuthObj.computeIfAbsent(target.getAuthGroup(), key -> new JSONArray());
                JSONArray auths = userRoleAuthObj.getJSONArray(target.getAuthGroup());
                if (!auths.contains(target.getAuthName())) {
                    auths.add(target.getAuthName());
                }
                RoleAuthVo targetAuth = new RoleAuthVo();
                targetAuth.setAuthGroup(target.getAuthGroup());
                targetAuth.setAuth(target.getAuthName());
                for (SourcePathVo source : rootSources.getOrDefault(rootName, Collections.emptyList())) {
                    addSourcePath(sources, targetAuth, new IncludeSourcePathVo(source, entry.getValue()));
                }
            }
        }
    }

    /** 每个根广度优先遍历，邻接编码排序使等长路径稳定；先访问去重阻止自包含和循环。 */
    private Map<AuthBase, List<JSONObject>> getIncludeAuthPaths(AuthBase root) {
        Map<AuthBase, List<JSONObject>> paths = new LinkedHashMap<>();
        Set<String> visited = new LinkedHashSet<>();
        visited.add(root.getAuthName());
        Queue<List<AuthBase>> queue = new ArrayDeque<>();
        queue.add(Collections.singletonList(root));
        while (!queue.isEmpty()) {
            List<AuthBase> path = queue.remove();
            AuthBase current = path.get(path.size() - 1);
            List<AuthBase> children = new ArrayList<>();
            for (Class<? extends AuthBase> childClass : CollectionUtils.emptyIfNull(current.getIncludeAuths())) {
                AuthBase child = childClass == null ? null : AuthFactory.getAuthInstance(childClass.getSimpleName());
                if (child != null) {
                    children.add(child);
                }
            }
            children.sort(Comparator.comparing(AuthBase::getAuthName));
            for (AuthBase child : children) {
                if (!visited.add(child.getAuthName())) {
                    continue;
                }
                List<AuthBase> childPath = new ArrayList<>(path);
                childPath.add(child);
                List<JSONObject> authPath = new ArrayList<>();
                for (AuthBase node : childPath) {
                    JSONObject item = new JSONObject();
                    item.put("authGroup", node.getAuthGroup());
                    item.put("auth", node.getAuthName());
                    authPath.add(item);
                }
                paths.put(child, authPath);
                queue.add(childPath);
            }
        }
        return paths;
    }

    /** 包含来源按根权限隔离；旧角色来源使用空标识，不改变其既有去重行为。 */
    private String getSourceRootKey(SourcePathVo source) {
        if (source instanceof IncludeSourcePathVo) {
            JSONObject root = ((IncludeSourcePathVo) source).getIncludeAuthPathList().get(0);
            return root.getString("authGroup") + "/" + root.getString("auth");
        }
        return "";
    }

    /** 缺失路径时按祖先层级重建；资料不完整时留空，由前端回退原只读提示。 */
    private List<String> getTeamSourcePath(TeamVo team) {
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
        return team.getUuid().equals(previous.getUuid()) ? names : null;
    }

    /** 角色来源按角色和分组去重，包含来源额外按根权限隔离，保留多个不同授权根。 */
    private void addSourcePath(Map<String, Map<String, List<SourcePathVo>>> sources, RoleAuthVo auth, SourcePathVo source) {
        if (StringUtils.isBlank(auth.getAuthGroup()) || StringUtils.isBlank(auth.getAuth())) {
            return;
        }
        List<SourcePathVo> paths = sources.computeIfAbsent(auth.getAuthGroup(), key -> new TreeMap<>())
                .computeIfAbsent(auth.getAuth(), key -> new ArrayList<>());
        if (paths.stream().noneMatch(path -> Objects.equals(path.getRoleUuid(), source.getRoleUuid())
                && Objects.equals(path.getTeamUuid(), source.getTeamUuid())
                && Objects.equals(getSourceRootKey(path), getSourceRootKey(source)))) {
            paths.add(source);
        }
    }

    /** 来源仅在本接口使用；局部序列化配置确保HTTP返回保留直接角色的null分组字段。 */
    @JSONType(serialzeFeatures = SerializerFeature.WriteMapNullValue)
    public static class SourcePathVo {
        private final String roleUuid;
        private final String roleName;
        private final String teamUuid;
        private final List<String> teamPathNameList;
        private final Integer teamLft;

        /** 保存原始名称和用户所属分组信息，不拼接中文展示文案。 */
        public SourcePathVo(String roleUuid, String roleName, String teamUuid, List<String> teamPathNameList, Integer teamLft) {
            this.roleUuid = roleUuid;
            this.roleName = roleName;
            this.teamUuid = teamUuid;
            this.teamPathNameList = teamPathNameList;
            this.teamLft = teamLft;
        }

        public String getRoleUuid() {
            return roleUuid;
        }

        public String getRoleName() {
            return roleName;
        }

        public String getTeamUuid() {
            return teamUuid;
        }

        public List<String> getTeamPathNameList() {
            return teamPathNameList;
        }

        public Integer getTeamLft() {
            return teamLft;
        }
    }

    /** 单独的包含来源类型确保旧角色来源JSON完全不出现includeAuthPathList字段。 */
    @JSONType(serialzeFeatures = SerializerFeature.WriteMapNullValue)
    public static class IncludeSourcePathVo extends SourcePathVo {
        private final List<JSONObject> includeAuthPathList;

        /** 复制原始来源身份并附带根到目标的最短包含链，不拼接展示文案。 */
        public IncludeSourcePathVo(SourcePathVo source, List<JSONObject> includeAuthPathList) {
            super(source.getRoleUuid(), source.getRoleName(), source.getTeamUuid(), source.getTeamPathNameList(), source.getTeamLft());
            this.includeAuthPathList = includeAuthPathList;
        }

        public List<JSONObject> getIncludeAuthPathList() {
            return includeAuthPathList;
        }
    }
}
