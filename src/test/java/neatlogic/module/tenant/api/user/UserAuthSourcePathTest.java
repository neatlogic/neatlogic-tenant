package neatlogic.module.tenant.api.user;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import neatlogic.framework.auth.core.AuthBase;
import neatlogic.framework.auth.core.AuthFactory;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.RoleAuthVo;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserAuthVo;
import org.junit.Assert;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/** 使用内存 Mapper 验证完整查询链，不依赖或改写本地数据库。 */
public class UserAuthSourcePathTest {
    private final List<RoleAuthVo> directAuths = new ArrayList<>();
    private final List<RoleAuthVo> teamAuths = new ArrayList<>();
    private final List<UserAuthVo> userAuths = new ArrayList<>();
    private final List<TeamVo> userTeams = new ArrayList<>();
    private final List<TeamVo> detailedTeams = new ArrayList<>();
    private final List<RoleVo> directTeamRoles = new ArrayList<>();
    private final List<RoleVo> namedRoles = new ArrayList<>();
    private final Map<String, List<RoleVo>> ancestorRoles = new HashMap<>();
    private final Map<Integer, List<TeamVo>> ancestors = new HashMap<>();
    private int pathQueryCount;
    private int roleNameQueryCount;
    private UserAuthSearchApi api;
    private Map<String, AuthBase> registry;
    private Map<String, AuthBase> registryBackup;

    /** 仅允许预期读取方法，发现额外写入或逐权限查询时使测试失败。 */
    @Before
    public void setup() throws Exception {
        api = new UserAuthSearchApi();
        inject("userMapper", Proxy.newProxyInstance(UserMapper.class.getClassLoader(), new Class<?>[]{UserMapper.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "searchUserAuthByUserUuid": return userAuths;
                        case "searchUserRoleAuthByUserUuid": return directAuths;
                        default: throw new AssertionError(method.getName());
                    }
                }));
        inject("teamMapper", Proxy.newProxyInstance(TeamMapper.class.getClassLoader(), new Class<?>[]{TeamMapper.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getTeamListByUserUuid": return userTeams;
                        case "getTeamByUuidList": return detailedTeams;
                        case "getAncestorsAndSelfByLftRht":
                            pathQueryCount++;
                            return ancestors.getOrDefault(args[0], Collections.emptyList());
                        default: throw new AssertionError(method.getName());
                    }
                }));
        inject("roleMapper", Proxy.newProxyInstance(RoleMapper.class.getClassLoader(), new Class<?>[]{RoleMapper.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getParentTeamRoleListWithCheckedChildrenByTeam":
                            return ancestorRoles.getOrDefault(((TeamVo) args[0]).getUuid(), Collections.emptyList());
                        case "getRoleListWithTeamByTeamUuidList": return directTeamRoles;
                        case "searchRoleAuthByRoleUuidList": return teamAuths;
                        case "getRoleByUuidList":
                            roleNameQueryCount++;
                            return namedRoles;
                        default: throw new AssertionError(method.getName());
                    }
                }));
    }

    /** 重复角色授权保留全部来源，直接角色优先，同类排序且不同权限组相互独立。 */
    @Test
    public void shouldMergeDirectAndTeamSourcesWithoutChangingExistingFields() throws Exception {
        namedRoles.add(role("z", "管理员", null));
        namedRoles.add(role("a", "值班角色", null));
        directAuths.add(auth("z", "process", "BASE"));
        directAuths.add(auth("a", "process", "BASE"));
        directAuths.add(auth("z", "process", "BASE"));
        directAuths.add(auth("a", "cmdb", "BASE"));
        TeamVo root = team("root", "苏州银行", "苏州银行", 1);
        TeamVo leaf = team("leaf", "金融同业部", "苏州银行/金融市场总部/金融同业部", 3);
        userTeams.addAll(Arrays.asList(root, leaf));
        detailedTeams.addAll(userTeams);
        directTeamRoles.add(role("z", "管理员", Arrays.asList(root, leaf)));
        ancestorRoles.put("leaf", Collections.singletonList(role("z", "管理员", Collections.singletonList(root))));
        teamAuths.add(auth("z", "process", "BASE"));
        teamAuths.add(auth("z", "process", "BASE"));
        userAuths.add(userAuth("process", "BASE"));
        userAuths.add(userAuth("process", "EDIT"));
        JSONObject result = search();
        Assert.assertEquals(Arrays.asList(source("a", "值班角色", null, null, null),
                source("z", "管理员", null, null, null), source("z", "管理员", "root", "苏州银行", 1),
                source("z", "管理员", "leaf", "苏州银行/金融市场总部/金融同业部", 3)), paths(result, "process", "BASE"));
        Assert.assertEquals(Collections.singletonList(source("a", "值班角色", null, null, null)), paths(result, "cmdb", "BASE"));
        // 保留旧接口跨组同名权限时原本的数组内容，不在提示优化中修正去重规则。
        Assert.assertEquals(JSONObject.parseObject("{process:['BASE','BASE'],cmdb:['BASE']}"), result.getJSONObject("userRoleAuthObj"));
        Assert.assertEquals(JSONObject.parseObject("{process:['EDIT']}"), result.getJSONObject("userAuthObj"));
        Assert.assertEquals(new JSONObject(), result.getJSONObject("userCodeAuthObj"));
        Assert.assertEquals(1, roleNameQueryCount);
        Assert.assertEquals(0, pathQueryCount);
    }

    /** 上级角色使用用户所属叶分组路径，缺失缓存路径时每个分组只重建一次。 */
    @Test
    public void shouldUseMembershipPathForAncestorRolesAndCacheFallback() throws Exception {
        TeamVo root = team("root", "苏州银行", null, 1);
        TeamVo leaf = team("leaf", "金融同业部", null, 2);
        leaf.setParentUuid("root");
        userTeams.add(leaf);
        detailedTeams.add(leaf);
        ancestors.put(2, Arrays.asList(root, leaf));
        namedRoles.add(role("r", "系统_管理员", null));
        ancestorRoles.put("leaf", Collections.singletonList(role("r", "系统_管理员", Collections.singletonList(root))));
        teamAuths.add(auth("r", "process", "BASE"));
        teamAuths.add(auth("r", "process", "EDIT"));
        JSONObject result = search();
        Assert.assertEquals(Collections.singletonList(source("r", "系统_管理员", "leaf", "苏州银行/金融同业部", 2)),
                paths(result, "process", "BASE"));
        Assert.assertEquals(paths(result, "process", "BASE"), paths(result, "process", "EDIT"));
        Assert.assertEquals(1, pathQueryCount);
    }

    /** 缺失角色名、分组路径或完整祖先链不拼造来源，旧权限照常回显。 */
    @Test
    public void shouldOmitUntrustworthySources() throws Exception {
        directAuths.add(auth("missing", "process", "DIRECT"));
        namedRoles.add(role("r", "管理员", null));
        TeamVo leaf = team("leaf", "叶分组", null, 3);
        TeamVo wrongParent = team("wrong", "无关分组", null, 1);
        leaf.setParentUuid("missing-parent");
        userTeams.add(leaf);
        detailedTeams.add(leaf);
        ancestors.put(3, Arrays.asList(wrongParent, leaf));
        directTeamRoles.add(role("r", "管理员", Collections.singletonList(leaf)));
        teamAuths.add(auth("r", "process", "BASE"));
        JSONObject result = search();
        Assert.assertTrue(result.getJSONArray("userRoleAuthSourcePathList").isEmpty());
        Assert.assertEquals(Arrays.asList("DIRECT", "BASE"), result.getJSONObject("userRoleAuthObj").getJSONArray("process"));
    }

    /** 没有角色授权时返回空列表，保持用户直接授权及代码授权结果。 */
    @Test
    public void shouldReturnEmptySourcesWithoutRoles() throws Exception {
        userAuths.add(userAuth("process", "EDIT"));
        JSONObject result = search();
        Assert.assertEquals(new JSONArray(), result.getJSONArray("userRoleAuthSourcePathList"));
        Assert.assertEquals(new JSONObject(), result.getJSONObject("userRoleAuthObj"));
        Assert.assertEquals(JSONObject.parseObject("{process:['EDIT']}"), result.getJSONObject("userAuthObj"));
        Assert.assertEquals(new JSONObject(), result.getJSONObject("userCodeAuthObj"));
        Assert.assertEquals(0, roleNameQueryCount);
    }

    /** 查询缺少最顶层祖先时，即使末端连续也不能把残缺路径返回给页面。 */
    @Test
    public void shouldRejectPartialAncestorChain() throws Exception {
        TeamVo leaf = team("leaf", "金融同业部", null, 3);
        leaf.setParentUuid("missing-root");
        userTeams.add(leaf);
        detailedTeams.add(leaf);
        ancestors.put(3, Collections.singletonList(leaf));
        namedRoles.add(role("r", "管理员", null));
        directTeamRoles.add(role("r", "管理员", Collections.singletonList(leaf)));
        teamAuths.add(auth("r", "process", "BASE"));
        JSONObject result = search();
        Assert.assertTrue(result.getJSONArray("userRoleAuthSourcePathList").isEmpty());
        Assert.assertEquals(Collections.singletonList("BASE"), result.getJSONObject("userRoleAuthObj").getJSONArray("process"));
    }

    /** 角色与分组名称及特殊字符原样返回，不增删中文后缀。 */
    @Test
    public void shouldPreserveNamesWithSuffixesAndSpecialCharacters() throws Exception {
        TeamVo team = team("t", "<银行>分组", "<银行>分组", 1);
        userTeams.add(team);
        detailedTeams.add(team);
        namedRoles.add(role("r", "系统_管理员角色", null));
        directTeamRoles.add(role("r", "系统_管理员角色", Collections.singletonList(team)));
        teamAuths.add(auth("r", "process", "BASE"));
        Assert.assertEquals(Collections.singletonList(source("r", "系统_管理员角色", "t", "<银行>分组", 1)),
                paths(search(), "process", "BASE"));
    }

    /** 直接角色同名不同UUID全部保留，分组顺序按lft而非路径文字，相同lft按UUID排序。 */
    @Test
    public void shouldSortByTreeOrderAndKeepDistinctIdentities() throws Exception {
        namedRoles.add(role("z", "相同角色", null));
        namedRoles.add(role("a", "相同角色", null));
        directAuths.add(auth("z", "process", "BASE"));
        directAuths.add(auth("a", "process", "BASE"));
        directAuths.add(auth("z", "process", "BASE"));
        TeamVo early = team("z-early", "Z分组", "Z分组", 1);
        TeamVo late = team("a-late", "A分组", "A分组", 2);
        TeamVo sameLeft = team("b-late", "B分组", "B分组", 2);
        TeamVo missingLeft = team("a-null", "C分组", "C分组", 3);
        missingLeft.setLft(null);
        TeamVo secondMissingLeft = team("b-null", "D分组", "D分组", 4);
        secondMissingLeft.setLft(null);
        userTeams.addAll(Arrays.asList(secondMissingLeft, sameLeft, late, missingLeft, early));
        detailedTeams.addAll(userTeams);
        directTeamRoles.add(role("z", "相同角色", userTeams));
        directTeamRoles.add(role("a", "相同角色", Collections.singletonList(late)));
        teamAuths.add(auth("z", "process", "BASE"));
        teamAuths.add(auth("a", "process", "BASE"));
        teamAuths.add(auth("z", "process", "BASE"));
        List<JSONObject> rows = paths(search(), "process", "BASE");
        Assert.assertEquals(Arrays.asList(source("a", "相同角色", null, null, null),
                source("z", "相同角色", null, null, null), source("z", "相同角色", "z-early", "Z分组", 1),
                source("a", "相同角色", "a-late", "A分组", 2), source("z", "相同角色", "a-late", "A分组", 2),
                source("z", "相同角色", "b-late", "B分组", 2), source("z", "相同角色", "a-null", "C分组", null),
                source("z", "相同角色", "b-null", "D分组", null)), rows);
        Assert.assertTrue(rows.get(0).containsKey("teamUuid"));
        Assert.assertTrue(rows.get(0).containsKey("teamLft"));
        Assert.assertTrue(rows.get(6).containsKey("teamLft"));
        Assert.assertNull(rows.get(6).get("teamLft"));
    }

    /** 同一分组内先按原始角色名再按UUID排序，不受Mapper返回顺序影响。 */
    @Test
    public void shouldSortRolesWithinTeamByRawNameThenUuid() throws Exception {
        TeamVo team = team("t", "银行", "银行", 1);
        userTeams.add(team);
        detailedTeams.add(team);
        for (String uuid : Arrays.asList("z", "m", "a")) {
            String name = "m".equals(uuid) ? "A管理员" : "B管理员";
            namedRoles.add(role(uuid, name, null));
            directTeamRoles.add(role(uuid, name, Collections.singletonList(team)));
            teamAuths.add(auth(uuid, "process", "BASE"));
        }
        Assert.assertEquals(Arrays.asList(source("m", "A管理员", "t", "银行", 1),
                source("a", "B管理员", "t", "银行", 1), source("z", "B管理员", "t", "银行", 1)),
                paths(search(), "process", "BASE"));
    }

    /** 直接根递归包含跨组权限；被包含权限已有直接授权时仍保留在userAuthObj。 */
    @Test
    public void shouldExpandDirectIncludesAndPreserveDirectTargets() throws Exception {
        installGraph();
        userAuths.add(userAuth("process", "INC_ROOT"));
        userAuths.add(userAuth("cmdb", "INC_TARGET"));
        JSONObject result = search();
        Assert.assertEquals(Arrays.asList("INC_LEFT", "INC_RIGHT"), result.getJSONObject("userRoleAuthObj").getJSONArray("process"));
        Assert.assertEquals(Collections.singletonList("INC_TARGET"), result.getJSONObject("userAuthObj").getJSONArray("cmdb"));
        Assert.assertEquals(Collections.singletonList("INC_DEEP"), result.getJSONObject("userRoleAuthObj").getJSONArray("autoexec"));
        JSONObject included = paths(result, "cmdb", "INC_TARGET").get(0);
        Assert.assertEquals(Arrays.asList("INC_ROOT", "INC_LEFT", "INC_TARGET"), includeNames(included));
        Assert.assertTrue(included.containsKey("roleUuid"));
        Assert.assertNull(included.get("roleUuid"));
        Assert.assertTrue(included.containsKey("teamUuid"));
        Assert.assertNull(included.get("teamUuid"));
        Assert.assertEquals(2, paths(result, "autoexec", "INC_DEEP").size());
        Assert.assertEquals(new JSONObject(), result.getJSONObject("userCodeAuthObj"));
    }

    /** 合并完成后再生成来源，返回对象及内部数组内容、顺序和引用均不改变，包含链只计算一次。 */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldKeepMergedRoleAuthUnchangedWhileBuildingSources() throws Exception {
        installGraph();
        userAuths.add(userAuth("process", "INC_ROOT"));
        directAuths.add(auth("r", "process", "INC_SECOND_ROOT"));
        directAuths.add(auth("r", "process", "INC_ROOT"));
        teamAuths.add(auth("r", "process", "INC_ROOT"));
        namedRoles.add(role("r", "管理员", null));
        TeamVo leaf = team("leaf", "叶分组", "银行/叶分组", 2);
        detailedTeams.add(leaf);
        Map<String, Set<String>> roleTeams = Collections.singletonMap("r", Collections.singleton("leaf"));

        // 与接口相同，三类数据库根先去重计算，再将目标权限合并到尚未返回的角色集合。
        Method getPaths = UserAuthSearchApi.class.getDeclaredMethod("getIncludedAuthPathMap", List.class, List.class, List.class);
        getPaths.setAccessible(true);
        Map<String, Map<AuthBase, List<JSONObject>>> includePaths =
                (Map<String, Map<AuthBase, List<JSONObject>>>) getPaths.invoke(api, userAuths, directAuths, teamAuths);
        Assert.assertEquals(Arrays.asList("INC_ROOT", "INC_SECOND_ROOT"), new ArrayList<>(includePaths.keySet()));
        INC_ROOT root = (INC_ROOT) registry.get("INC_ROOT");
        Assert.assertEquals(1, root.includeReadCount);
        JSONObject roleAuthObj = JSONObject.parseObject("{process:['INC_SECOND_ROOT','INC_ROOT']}");
        Method merge = UserAuthSearchApi.class.getDeclaredMethod("mergeIncludedAuths", JSONObject.class, Map.class);
        merge.setAccessible(true);
        merge.invoke(api, roleAuthObj, includePaths);
        Assert.assertEquals(JSONObject.parseObject("{process:['INC_SECOND_ROOT','INC_ROOT','INC_LEFT','INC_RIGHT'],"
                + "cmdb:['INC_TARGET'],autoexec:['INC_DEEP']}"), roleAuthObj);

        JSONObject result = new JSONObject();
        result.put("userRoleAuthObj", roleAuthObj);
        JSONObject snapshot = JSONObject.parseObject(roleAuthObj.toJSONString());
        Map<String, JSONArray> arrays = new HashMap<>();
        roleAuthObj.keySet().forEach(group -> arrays.put(group, roleAuthObj.getJSONArray(group)));
        // 来源方法不再接收权限集合，仅使用缓存链和角色、分组资料生成说明。
        Method getSources = UserAuthSearchApi.class.getDeclaredMethod("getRoleAuthSourcePathList",
                List.class, List.class, List.class, Map.class, Map.class);
        getSources.setAccessible(true);
        JSONArray sources = (JSONArray) getSources.invoke(api, userAuths, directAuths, teamAuths, roleTeams, includePaths);
        result.put("userRoleAuthSourcePathList", sources);
        Assert.assertSame(roleAuthObj, result.getJSONObject("userRoleAuthObj"));
        Assert.assertEquals(snapshot, result.getJSONObject("userRoleAuthObj"));
        arrays.forEach((group, array) -> Assert.assertSame(array, roleAuthObj.getJSONArray(group)));
        Assert.assertEquals(1, root.includeReadCount);
        JSONObject serialized = JSONObject.parseObject(result.toString(SerializerFeature.DisableCircularReferenceDetect));
        Assert.assertEquals(4, paths(serialized, "cmdb", "INC_TARGET").size());
    }

    /** 角色旧来源保持五字段并优先，包含来源依次为用户直接、直接角色和所属分组。 */
    @Test
    public void shouldKeepLegacySourcesBeforeIncludedRoleAndTeamSources() throws Exception {
        installGraph();
        userAuths.add(userAuth("process", "INC_ROOT"));
        namedRoles.add(role("r", "根角色", null));
        namedRoles.add(role("legacy", "目标角色", null));
        directAuths.add(auth("r", "process", "INC_ROOT"));
        directAuths.add(auth("legacy", "cmdb", "INC_TARGET"));
        TeamVo leaf = team("leaf", "叶分组", "银行/叶分组", 2);
        userTeams.add(leaf);
        detailedTeams.add(leaf);
        ancestorRoles.put("leaf", Collections.singletonList(role("r", "根角色", null)));
        teamAuths.add(auth("r", "process", "INC_ROOT"));
        JSONObject result = search();
        List<JSONObject> rows = paths(result, "cmdb", "INC_TARGET");
        Assert.assertEquals(4, rows.size());
        Assert.assertFalse(rows.get(0).containsKey("includeAuthPathList"));
        Assert.assertEquals(source("legacy", "目标角色", null, null, null), rows.get(0));
        Assert.assertNull(rows.get(1).get("roleUuid"));
        Assert.assertEquals("r", rows.get(2).getString("roleUuid"));
        Assert.assertNull(rows.get(2).get("teamUuid"));
        Assert.assertEquals("leaf", rows.get(3).getString("teamUuid"));
        Assert.assertEquals(Arrays.asList("银行", "叶分组"), rows.get(3).getJSONArray("teamPathNameList"));
        Assert.assertEquals(Arrays.asList("INC_ROOT", "INC_LEFT", "INC_TARGET"), includeNames(rows.get(3)));
    }

    /** 来源资料缺失时仍展开只读对象，不借用其他角色或分组伪造说明。 */
    @Test
    public void shouldExpandIncludesEvenWhenRoleMetadataIsMissing() throws Exception {
        installGraph();
        directAuths.add(auth("missing", "process", "INC_ROOT"));
        JSONObject result = search();
        Assert.assertTrue(result.getJSONObject("userRoleAuthObj").getJSONArray("cmdb").contains("INC_TARGET"));
        Assert.assertTrue(result.getJSONObject("userRoleAuthObj").getJSONArray("autoexec").contains("INC_DEEP"));
        Assert.assertTrue(result.getJSONArray("userRoleAuthSourcePathList").isEmpty());
    }

    /** 每根每目标仅保留最短链，等长菱形链选编码较小分支，循环、自包含及未注册节点跳过。 */
    @Test
    public void shouldChooseShortestStableChainsAndRejectCycles() throws Exception {
        installGraph();
        userAuths.add(userAuth("process", "INC_ROOT"));
        userAuths.add(userAuth("process", "INC_SHORT_ROOT"));
        userAuths.add(userAuth("process", "INC_UNREGISTERED"));
        JSONObject result = search();
        List<JSONObject> rows = paths(result, "cmdb", "INC_TARGET");
        Assert.assertEquals(2, rows.size());
        Assert.assertEquals(Arrays.asList("INC_ROOT", "INC_LEFT", "INC_TARGET"), includeNames(rows.get(0)));
        Assert.assertEquals(Arrays.asList("INC_SHORT_ROOT", "INC_TARGET"), includeNames(rows.get(1)));
        Assert.assertFalse(result.getJSONObject("userRoleAuthObj").getJSONArray("process").contains("INC_UNREGISTERED"));
        // ROOT只会因为SHORT_ROOT包含它而只读，不允许ROOT自己产生ROOT->ROOT来源。
        List<JSONObject> rootRows = paths(result, "process", "INC_ROOT");
        Assert.assertEquals(1, rootRows.size());
        Assert.assertEquals(Arrays.asList("INC_SHORT_ROOT", "INC_ROOT"), includeNames(rootRows.get(0)));
    }

    /** 同一来源从多个数据库根获得目标时分别保留，重复的同根数据库记录只出现一次。 */
    @Test
    public void shouldKeepMultipleRootsAndDeduplicateRepeatedDatabaseRows() throws Exception {
        installGraph();
        namedRoles.add(role("r", "管理员", null));
        for (String root : Arrays.asList("INC_SECOND_ROOT", "INC_ROOT", "INC_ROOT")) {
            directAuths.add(auth("r", "process", root));
        }
        List<JSONObject> rows = paths(search(), "cmdb", "INC_TARGET");
        Assert.assertEquals(2, rows.size());
        Assert.assertEquals(Arrays.asList("INC_ROOT", "INC_LEFT", "INC_TARGET"), includeNames(rows.get(0)));
        Assert.assertEquals(Arrays.asList("INC_SECOND_ROOT", "INC_TARGET"), includeNames(rows.get(1)));
    }

    /** 独立验证附加autoexec-base及cmdb-base类目录，检查真实注册权限定义的递归链。 */
    @Test
    public void shouldExpandRealAutoexecIncludesWhenModuleIsAvailable() throws Exception {
        Class<?> rootClass;
        try {
            rootClass = Class.forName("neatlogic.framework.autoexec.auth.AUTOEXEC_MODIFY");
        } catch (ClassNotFoundException e) {
            Assume.assumeNoException("tenant未依赖autoexec-base，独立模块验证时启用", e);
            return;
        }
        installGraph();
        registerRealAuthGraph((AuthBase) rootClass.getDeclaredConstructor().newInstance(), new HashSet<>());
        userAuths.add(userAuth("autoexec", "AUTOEXEC_MODIFY"));
        JSONObject result = search();
        Assert.assertTrue(result.getJSONObject("userRoleAuthObj").getJSONArray("autoexec").contains("AUTOEXEC_SCRIPT_SEARCH"));
        JSONObject row = paths(result, "autoexec", "AUTOEXEC_SCRIPT_SEARCH").get(0);
        Assert.assertEquals(Arrays.asList("AUTOEXEC_MODIFY", "AUTOEXEC_SCRIPT_MANAGE",
                "AUTOEXEC_SCRIPT_MODIFY", "AUTOEXEC_SCRIPT_SEARCH"), includeNames(row));
        Assert.assertTrue(result.getJSONObject("userRoleAuthObj").getJSONArray("cmdb").contains("CMDB"));
    }

    /** 只替换测试注册项并在结束时恢复，不影响其他授权回归测试的静态注册表。 */
    @SuppressWarnings("unchecked")
    private void installGraph() throws Exception {
        Field field = AuthFactory.class.getDeclaredField("authMap");
        field.setAccessible(true);
        registry = (Map<String, AuthBase>) field.get(null);
        registryBackup = new HashMap<>(registry);
        for (AuthBase auth : Arrays.asList(new INC_ROOT(), new INC_LEFT(), new INC_RIGHT(), new INC_TARGET(),
                new INC_DEEP(), new INC_SECOND_ROOT(), new INC_SHORT_ROOT())) {
            registry.put(auth.getAuthName(), auth);
        }
        registry.remove("INC_UNREGISTERED");
    }

    @After
    public void restoreAuthRegistry() {
        if (registryBackup != null) {
            registry.clear();
            registry.putAll(registryBackup);
        }
    }

    /** 按真实权限类的包含声明注册递归依赖，不给生产模块添加依赖。 */
    private void registerRealAuthGraph(AuthBase auth, Set<String> visited) throws Exception {
        if (!visited.add(auth.getAuthName())) {
            return;
        }
        registry.put(auth.getAuthName(), auth);
        for (Class<? extends AuthBase> included : auth.getIncludeAuths()) {
            registerRealAuthGraph(included.getDeclaredConstructor().newInstance(), visited);
        }
    }

    private List<String> includeNames(JSONObject row) {
        List<String> names = new ArrayList<>();
        JSONArray path = row.getJSONArray("includeAuthPathList");
        for (int i = 0; i < path.size(); i++) {
            names.add(path.getJSONObject(i).getString("auth"));
        }
        return names;
    }

    /** 合成最小权限图，使用真实AuthFactory注册实例验证遍历而不依赖业务模块。 */
    public abstract static class IncludeTestAuth extends AuthBase {
        @Override public String getAuthDisplayName() { return getAuthName(); }
        @Override public String getAuthIntroduction() { return getAuthName(); }
        @Override public String getAuthGroup() { return "process"; }
        @Override public Integer getSort() { return 0; }
    }
    public static class INC_ROOT extends IncludeTestAuth {
        // 记录真实注册实例的读取次数，验证重复数据库根去重及来源组装复用缓存链。
        private int includeReadCount;
        @Override public List<Class<? extends AuthBase>> getIncludeAuths() {
            includeReadCount++;
            return Arrays.asList(INC_RIGHT.class, INC_LEFT.class, INC_ROOT.class, INC_UNREGISTERED.class);
        }
    }
    public static class INC_LEFT extends IncludeTestAuth {
        @Override public List<Class<? extends AuthBase>> getIncludeAuths() { return Arrays.asList(INC_TARGET.class, INC_ROOT.class); }
    }
    public static class INC_RIGHT extends IncludeTestAuth {
        @Override public List<Class<? extends AuthBase>> getIncludeAuths() { return Collections.singletonList(INC_TARGET.class); }
    }
    public static class INC_TARGET extends IncludeTestAuth {
        @Override public String getAuthGroup() { return "cmdb"; }
        @Override public List<Class<? extends AuthBase>> getIncludeAuths() { return Collections.singletonList(INC_DEEP.class); }
    }
    public static class INC_DEEP extends IncludeTestAuth {
        @Override public String getAuthGroup() { return "autoexec"; }
    }
    public static class INC_SECOND_ROOT extends IncludeTestAuth {
        @Override public List<Class<? extends AuthBase>> getIncludeAuths() { return Collections.singletonList(INC_TARGET.class); }
    }
    public static class INC_SHORT_ROOT extends IncludeTestAuth {
        @Override public List<Class<? extends AuthBase>> getIncludeAuths() { return Arrays.asList(INC_ROOT.class, INC_TARGET.class); }
    }
    public static class INC_UNREGISTERED extends IncludeTestAuth { }

    /** 构造预期HTTP结构，显式保留null键并校验字段集合。 */
    private JSONObject source(String roleUuid, String roleName, String teamUuid, String path, Integer lft) {
        JSONObject row = new JSONObject();
        row.put("roleUuid", roleUuid);
        row.put("roleName", roleName);
        row.put("teamUuid", teamUuid);
        row.put("teamPathNameList", path == null ? Collections.emptyList() : Arrays.asList(path.split("/", -1)));
        row.put("teamLft", lft);
        return row;
    }

    /** 以下辅助方法构造最小 Mapper 数据并定位按组匹配的接口来源记录。 */
    private JSONObject search() throws Exception {
        JSONObject request = new JSONObject();
        request.put("userUuid", "source-path-test-ordinary-user");
        // 模拟Dispatcher的最终HTTP序列化配置，确认直接角色及缺失lft的null字段仍存在。
        JSONObject envelope = new JSONObject();
        envelope.put("Return", api.myDoService(request));
        return JSONObject.parseObject(envelope.toString(SerializerFeature.DisableCircularReferenceDetect)).getJSONObject("Return");
    }

    private List<JSONObject> paths(JSONObject result, String group, String auth) {
        JSONArray rows = result.getJSONArray("userRoleAuthSourcePathList");
        for (int i = 0; i < rows.size(); i++) {
            JSONObject row = rows.getJSONObject(i);
            if (group.equals(row.getString("authGroup")) && auth.equals(row.getString("auth"))) {
                return row.getJSONArray("sourcePathList").toJavaList(JSONObject.class);
            }
        }
        Assert.fail("缺少来源：" + group + "/" + auth);
        return Collections.emptyList();
    }

    private void inject(String fieldName, Object mapper) throws Exception {
        Field field = UserAuthSearchApi.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(api, mapper);
    }

    private RoleAuthVo auth(String roleUuid, String group, String name) {
        RoleAuthVo auth = new RoleAuthVo(roleUuid, name);
        auth.setAuthGroup(group);
        return auth;
    }

    private UserAuthVo userAuth(String group, String name) {
        UserAuthVo auth = new UserAuthVo();
        auth.setAuthGroup(group);
        auth.setAuth(name);
        return auth;
    }

    private RoleVo role(String uuid, String name, List<TeamVo> teams) {
        RoleVo role = new RoleVo();
        role.setUuid(uuid);
        role.setName(name);
        if (teams != null) {
            role.setTeamList(teams);
        }
        return role;
    }

    private TeamVo team(String uuid, String name, String path, int left) {
        TeamVo team = new TeamVo();
        team.setUuid(uuid);
        team.setName(name);
        team.setParentUuid(TeamVo.ROOT_UUID);
        team.setUpwardNamePath(path);
        team.setLft(left);
        team.setRht(10 - left);
        return team;
    }
}
