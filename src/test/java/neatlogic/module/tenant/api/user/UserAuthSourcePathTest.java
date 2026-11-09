package neatlogic.module.tenant.api.user;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.RoleAuthVo;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.dto.SourcePathVo;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserAuthVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.service.UserService;
import neatlogic.framework.service.UserServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;

/** 验证API的服务委托和返回组装，包含遍历及路径重建细节由framework服务测试覆盖。 */
public class UserAuthSourcePathTest {
    private static final String USER_UUID = "source-path-test-ordinary-user";
    private final List<UserAuthVo> records = new ArrayList<>();
    private UserAuthSearchApi api;
    private int serviceCalls;
    private String requestedUser;
    private boolean nullResult;
    private RuntimeException serviceFailure;

    /** 只允许调用统一授权查询，未声明的服务调用立即使测试失败。 */
    @Before
    public void setup() throws Exception {
        api = new UserAuthSearchApi();
        UserService service = (UserService) Proxy.newProxyInstance(UserService.class.getClassLoader(),
                new Class<?>[]{UserService.class}, (proxy, method, args) -> {
                    if (!"getUserAuthListByUserUuid".equals(method.getName())) {
                        throw new AssertionError("不应调用其他服务方法：" + method.getName());
                    }
                    serviceCalls++;
                    requestedUser = (String) args[0];
                    if (serviceFailure != null) {
                        throw serviceFailure;
                    }
                    return nullResult ? null : records;
                });
        inject(api, "userService", service);
    }

    /** 不经过旧Mapper逻辑，正确传递用户UUID且每次请求只委托一次，组内编码继续排序。 */
    @Test
    public void shouldDelegateOnceAndSortDirectPermissions() throws Exception {
        records.add(auth("process", "Z_DIRECT", Collections.emptyList()));
        records.add(auth("process", "A_DIRECT", null));
        records.add(auth("cmdb", "C_DIRECT", Collections.emptyList()));
        JSONObject result = result();
        Assert.assertEquals(Arrays.asList("A_DIRECT", "Z_DIRECT"), result.getJSONObject("userAuthObj").getJSONArray("process"));
        Assert.assertEquals(Collections.singletonList("C_DIRECT"), result.getJSONObject("userAuthObj").getJSONArray("cmdb"));
        Assert.assertTrue(result.getJSONObject("userRoleAuthObj").isEmpty());
        Assert.assertTrue(result.getJSONArray("userRoleAuthSourcePathList").isEmpty());
        assertDelegatedOnce();
    }

    /** 无直接权限或只读权限时仍返回四个字段，服务返回null也按空集合处理。 */
    @Test
    public void shouldHandleEmptyAndNullServiceResults() throws Exception {
        for (boolean missing : Arrays.asList(false, true)) {
            nullResult = missing;
            JSONObject result = result();
            Assert.assertTrue(result.getJSONObject("userAuthObj").isEmpty());
            Assert.assertTrue(result.getJSONObject("userRoleAuthObj").isEmpty());
            Assert.assertTrue(result.getJSONObject("userCodeAuthObj").isEmpty());
            Assert.assertTrue(result.getJSONArray("userRoleAuthSourcePathList").isEmpty());
        }
        Assert.assertEquals(2, serviceCalls);
        Assert.assertEquals(USER_UUID, requestedUser);
    }

    /** 角色、分组及包含来源均归为只读；权限编码排序，来源记录及包含链保留服务顺序。 */
    @Test
    public void shouldGroupReadOnlySourcesWithoutSortingSourceRecords() throws Exception {
        SourcePathVo role = source("r", "角色<一>&", null, Collections.emptyList());
        SourcePathVo team = source("r", "角色<一>&", "leaf", Arrays.asList("银行", "分组"));
        SourcePathVo include = source(null, null, null, Collections.emptyList());
        include.setIncludeAuthPathList(Arrays.asList(node("ROOT"), node("MIDDLE"), node("B_INCLUDED")));
        records.add(auth("process", "Z_ROLE", Collections.singletonList(role)));
        records.add(auth("process", "A_TEAM", Collections.singletonList(team)));
        records.add(auth("process", "B_INCLUDED", Collections.singletonList(include)));
        JSONObject result = result();
        Assert.assertTrue(result.getJSONObject("userAuthObj").isEmpty());
        Assert.assertEquals(Arrays.asList("A_TEAM", "B_INCLUDED", "Z_ROLE"), result.getJSONObject("userRoleAuthObj").getJSONArray("process"));
        JSONArray rows = result.getJSONArray("userRoleAuthSourcePathList");
        Assert.assertEquals(Arrays.asList("Z_ROLE", "A_TEAM", "B_INCLUDED"), Arrays.asList(
                rows.getJSONObject(0).getString("auth"), rows.getJSONObject(1).getString("auth"), rows.getJSONObject(2).getString("auth")));
        Assert.assertEquals("角色<一>&", rows.getJSONObject(0).getJSONArray("sourcePathList").getJSONObject(0).getString("roleName"));
        Assert.assertEquals(Arrays.asList("银行", "分组"), rows.getJSONObject(1).getJSONArray("sourcePathList").getJSONObject(0).getJSONArray("teamPathNameList"));
        Assert.assertEquals(include.getIncludeAuthPathList(), rows.getJSONObject(2).getJSONArray("sourcePathList").getJSONObject(0).getJSONArray("includeAuthPathList"));
        assertDelegatedOnce();
    }

    /** 统一服务已合并直接和包含身份时以其来源归类，旧实现差异不再导致接口返回null。 */
    @Test
    public void shouldUseServiceClassificationForIncludedDirectPermission() throws Exception {
        records.add(auth("process", "ROOT", Collections.emptyList()));
        SourcePathVo included = source(null, null, null, Collections.emptyList());
        included.setIncludeAuthPathList(Arrays.asList(node("ROOT"), node("TARGET")));
        records.add(auth("process", "TARGET", Collections.singletonList(included)));
        JSONObject result = result();
        Assert.assertEquals(Collections.singletonList("ROOT"), result.getJSONObject("userAuthObj").getJSONArray("process"));
        Assert.assertEquals(Collections.singletonList("TARGET"), result.getJSONObject("userRoleAuthObj").getJSONArray("process"));
        Assert.assertEquals(1, result.getJSONArray("userRoleAuthSourcePathList").size());
        assertDelegatedOnce();
    }

    /** 保留getResult现有处理：仅清除只读记录的userUuid，来源DTO和其他字段按原对象输出。 */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldClearUserUuidOnlyForReadOnlyRecords() throws Exception {
        UserAuthVo direct = auth("process", "DIRECT", Collections.emptyList());
        UserAuthVo inherited = auth("process", "INHERITED", Collections.singletonList(source("r", "角色", null, Collections.emptyList())));
        records.addAll(Arrays.asList(direct, inherited));
        JSONObject raw = (JSONObject) api.myDoService(request());
        Assert.assertEquals(USER_UUID, direct.getUserUuid());
        Assert.assertNull(inherited.getUserUuid());
        Assert.assertSame(inherited, ((List<UserAuthVo>) raw.get("userRoleAuthSourcePathList")).get(0));
        JSONObject wire = wire(raw);
        Assert.assertEquals(wire(inherited), wire.getJSONArray("userRoleAuthSourcePathList").getJSONObject(0));
        Assert.assertFalse(wire.getJSONArray("userRoleAuthSourcePathList").getJSONObject(0).containsKey("userUuid"));
        assertDelegatedOnce();
    }

    /** 服务异常沿现有调用链抛出，不吞异常或回退执行旧实现。 */
    @Test
    public void shouldPropagateServiceFailure() throws Exception {
        serviceFailure = new IllegalStateException("模拟服务失败");
        try {
            api.myDoService(request());
            Assert.fail("服务异常应继续抛出");
        } catch (IllegalStateException expected) {
            Assert.assertSame(serviceFailure, expected);
        }
        assertDelegatedOnce();
    }

    /** 真实服务与内存Mapper集成，验证API可回显用户、角色和所属分组来源而不连接数据库。 */
    @Test
    public void shouldIntegrateWithRealServiceAndInMemoryMappers() throws Exception {
        UserServiceImpl service = new UserServiceImpl();
        Map<String, Integer> queries = new HashMap<>();
        TeamVo team = new TeamVo();
        team.setUuid("leaf");
        team.setName("分组");
        team.setUpwardNamePath("银行/分组");
        team.setLft(2);
        team.setRht(3);
        RoleVo directRole = role("r", "直接角色", null);
        RoleVo teamRole = role("t", "分组角色", Collections.singletonList(team));
        inject(service, "userMapper", Proxy.newProxyInstance(UserMapper.class.getClassLoader(), new Class<?>[]{UserMapper.class},
                (proxy, method, args) -> {
                    queries.merge(method.getName(), 1, Integer::sum);
                    switch (method.getName()) {
                        // 真实服务先校验用户存在，再查询三类授权。
                        case "getUserByUuid":
                            UserVo user = new UserVo();
                            user.setUuid(USER_UUID);
                            return user;
                        case "searchUserAuthByUserUuid": return Collections.singletonList(auth("process", "Z_DIRECT", null));
                        case "searchUserRoleAuthByUserUuid": return Collections.singletonList(roleAuth("r", "A_ROLE"));
                        default: throw new AssertionError(method.getName());
                    }
                }));
        inject(service, "teamMapper", Proxy.newProxyInstance(TeamMapper.class.getClassLoader(), new Class<?>[]{TeamMapper.class},
                (proxy, method, args) -> {
                    queries.merge(method.getName(), 1, Integer::sum);
                    switch (method.getName()) {
                        case "getTeamListByUserUuid":
                        case "getTeamByUuidList": return Collections.singletonList(team);
                        default: throw new AssertionError(method.getName());
                    }
                }));
        inject(service, "roleMapper", Proxy.newProxyInstance(RoleMapper.class.getClassLoader(), new Class<?>[]{RoleMapper.class},
                (proxy, method, args) -> {
                    queries.merge(method.getName(), 1, Integer::sum);
                    switch (method.getName()) {
                        case "getParentTeamRoleListWithCheckedChildrenByTeam": return Collections.emptyList();
                        case "getRoleListWithTeamByTeamUuidList": return Collections.singletonList(teamRole);
                        case "searchRoleAuthByRoleUuidList": return Collections.singletonList(roleAuth("t", "B_TEAM"));
                        case "getRoleByUuidList": return Arrays.asList(directRole, teamRole);
                        default: throw new AssertionError(method.getName());
                    }
                }));
        inject(api, "userService", service);
        JSONObject result = result();
        Assert.assertEquals(Collections.singletonList("Z_DIRECT"), result.getJSONObject("userAuthObj").getJSONArray("process"));
        Assert.assertEquals(Arrays.asList("A_ROLE", "B_TEAM"), result.getJSONObject("userRoleAuthObj").getJSONArray("process"));
        JSONArray sources = result.getJSONArray("userRoleAuthSourcePathList");
        Assert.assertEquals(2, sources.size());
        Map<String, JSONObject> byAuth = new HashMap<>();
        sources.forEach(row -> byAuth.put(((JSONObject) row).getString("auth"), (JSONObject) row));
        Assert.assertEquals("r", byAuth.get("A_ROLE").getJSONArray("sourcePathList").getJSONObject(0).getString("roleUuid"));
        Assert.assertEquals("leaf", byAuth.get("B_TEAM").getJSONArray("sourcePathList").getJSONObject(0).getString("teamUuid"));
        Assert.assertEquals(Integer.valueOf(1), queries.get("searchUserAuthByUserUuid"));
        Assert.assertEquals(Integer.valueOf(1), queries.get("searchUserRoleAuthByUserUuid"));
        Assert.assertEquals(Integer.valueOf(1), queries.get("getTeamListByUserUuid"));
        Assert.assertEquals(Integer.valueOf(2), queries.get("getRoleByUuidList"));
    }

    /** 所有正常请求均应返回四个约定字段，按Dispatcher的循环引用配置检查实际JSON。 */
    private JSONObject result() throws Exception {
        Object value = api.myDoService(request());
        Assert.assertNotNull(value);
        JSONObject result = wire(value);
        Assert.assertEquals(new HashSet<>(Arrays.asList("userAuthObj", "userRoleAuthObj", "userCodeAuthObj", "userRoleAuthSourcePathList")), result.keySet());
        return result;
    }

    /** 统一请求使用非系统用户UUID，代码权限回归由系统用户测试覆盖。 */
    private JSONObject request() {
        return new JSONObject().fluentPut("userUuid", USER_UUID);
    }

    /** 普通路径应只调用一次服务，且不改变请求中的用户UUID。 */
    private void assertDelegatedOnce() {
        Assert.assertEquals(1, serviceCalls);
        Assert.assertEquals(USER_UUID, requestedUser);
    }

    /** 使用当前DTO配置序列化，不主动补齐空值或筛选字段。 */
    private JSONObject wire(Object value) {
        return JSONObject.parseObject(JSONObject.toJSONString(value, SerializerFeature.DisableCircularReferenceDetect));
    }

    /** 构造统一服务已聚合的权限记录，测试API层不再重复推导来源。 */
    private UserAuthVo auth(String group, String code, List<SourcePathVo> paths) {
        return new UserAuthVo(USER_UUID, code, group, paths);
    }

    /** 来源名称按原值保存，由前端处理国际化展示。 */
    private SourcePathVo source(String roleUuid, String roleName, String teamUuid, List<String> names) {
        return new SourcePathVo(roleUuid, roleName, teamUuid, names, teamUuid == null ? null : 2);
    }

    /** 包含链节点只保存编码与权限组，不拼接展示文案。 */
    private JSONObject node(String code) {
        return new JSONObject().fluentPut("authGroup", "process").fluentPut("auth", code);
    }

    /** 原始角色权限供真实服务的Mapper替身使用。 */
    private RoleAuthVo roleAuth(String uuid, String code) {
        RoleAuthVo auth = new RoleAuthVo(uuid, code);
        auth.setAuthGroup("process");
        return auth;
    }

    /** 角色及其分组绑定使用真实DTO，保持服务的资料完整性检查。 */
    private RoleVo role(String uuid, String name, List<TeamVo> teams) {
        RoleVo role = new RoleVo();
        role.setUuid(uuid);
        role.setName(name);
        role.setTeamList(teams);
        return role;
    }

    /** 测试注入服务及其Mapper，生产类无需暴露测试装配入口。 */
    private void inject(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
