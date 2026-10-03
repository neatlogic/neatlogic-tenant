package neatlogic.module.tenant.api.auth;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.RequestContext;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.ApiAuthContext;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.auth.core.AuthBase;
import neatlogic.framework.auth.core.AuthFactory;
import neatlogic.framework.auth.label.AUTHORITY_MODIFY;
import neatlogic.framework.auth.label.USER_MODIFY;
import neatlogic.framework.common.constvalue.systemuser.ISystemUser;
import neatlogic.framework.common.constvalue.systemuser.SystemUserFactory;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.dto.RoleAuthVo;
import neatlogic.framework.dto.UserAuthVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.auth.AuthNotFoundException;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.framework.service.UserService;
import neatlogic.framework.util.TimeUtil;
import neatlogic.module.tenant.api.user.UserAuthSaveApi;
import neatlogic.module.tenant.api.user.UserAuthSearchApi;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;

/** 权限管理系统用户接口回归，使用内存持久化替身，隔离真实租户授权数据。 */
public class AuthSystemUserApiTest {
    private final Map<String, Map<String, String>> authorityMap = new HashMap<>();
    private Map<String, ISystemUser> systemUserMap;
    private Map<String, ISystemUser> originalSystemUserMap;
    private Map<String, AuthBase> authMap;
    private Map<String, AuthBase> originalAuthMap;
    private Field checkerMapperField;
    private Object originalCheckerMapper;
    private UserMapper mapper;
    private AuthUserSaveApi saveApi;
    private AuthUserDeleteApi deleteApi;
    private SearchAuthSystemUserApi listApi;
    private int memberQueryCount;
    private int expandUserCount;
    private int writeCount;
    private String failureMethod;
    private String failureUuid;

    /** 安装 Mapper 与用户展开服务替身，保留全局注册表和鉴权对象供恢复。 */
    @Before
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        systemUserMap = (Map<String, ISystemUser>) field(SystemUserFactory.class, "systemUserMap").get(null);
        originalSystemUserMap = new HashMap<>(systemUserMap);
        authMap = (Map<String, AuthBase>) field(AuthFactory.class, "authMap").get(null);
        originalAuthMap = new HashMap<>(authMap);
        authMap.put("AUTHORITY_MODIFY", new AUTHORITY_MODIFY());
        authMap.put("USER_MODIFY", new USER_MODIFY());
        mapper = (UserMapper) Proxy.newProxyInstance(UserMapper.class.getClassLoader(), new Class<?>[]{UserMapper.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getUserUuidListByAuthAndUserUuidList":
                            memberQueryCount++;
                            String auth = (String) args[0];
                            List<String> ids = (List<String>) args[1];
                            Assert.assertFalse("不应把空 UUID 集合传给 IN 查询", ids.isEmpty());
                            return ids.stream().filter(uuid -> auths(uuid).contains(auth)).collect(Collectors.toList());
                        case "searchUserAuthByUserUuid":
                            return authVos((String) args[0]);
                        case "searchUserAllAuthByUserAuth":
                            return authVos(((AuthenticationInfoVo) args[0]).getUserUuid());
                        case "searchUserRoleAuthByUserUuid":
                            return Collections.emptyList();
                        case "checkUserIsExists":
                            if ("ordinary-user".equals(args[0])) {
                                return 1;
                            }
                            return 0;
                        case "insertUserAuth":
                        case "deleteUserAuth":
                            UserAuthVo value = (UserAuthVo) args[0];
                            if (Objects.equals(failureMethod, method.getName()) && Objects.equals(failureUuid, value.getUserUuid())) {
                                throw new IllegalStateException("模拟后续用户的授权写入失败");
                            }
                            writeCount++;
                            Map<String, String> values = authorityMap.computeIfAbsent(value.getUserUuid(), key -> new HashMap<>());
                            if ("insertUserAuth".equals(method.getName())) {
                                values.put(value.getAuth(), value.getAuthGroup());
                            } else if (value.getAuth() == null) {
                                values.clear();
                            } else {
                                values.remove(value.getAuth());
                            }
                            return 1;
                        default:
                            throw new AssertionError("不应调用额外的用户 Mapper 方法：" + method.getName());
                    }
                });
        UserService userService = (UserService) Proxy.newProxyInstance(UserService.class.getClassLoader(),
                new Class<?>[]{UserService.class}, (proxy, method, args) -> {
                    Assert.assertEquals("getUserUuidSetByUserUuidListAndTeamUuidList", method.getName());
                    expandUserCount++;
                    Set<String> uuids = new HashSet<>();
                    if (args[0] != null) {
                        uuids.addAll((List<String>) args[0]);
                    }
                    if (args[1] != null && ((List<String>) args[1]).contains("test-team")) {
                        uuids.add("team-member");
                    }
                    return uuids;
                });
        saveApi = new AuthUserSaveApi();
        inject(saveApi, "userMapper", mapper);
        inject(saveApi, "userService", userService);
        deleteApi = new AuthUserDeleteApi();
        inject(deleteApi, "userMapper", mapper);
        listApi = new SearchAuthSystemUserApi();
        inject(listApi, "userMapper", mapper);
        checkerMapperField = field(AuthActionChecker.class, "userMapper");
        originalCheckerMapper = checkerMapperField.get(null);
        checkerMapperField.set(null, mapper);
        TenantContext.init("test-tenant");
    }

    /** 恢复静态注册与线程状态，避免测试扩展身份影响其他测试或后续请求。 */
    @After
    public void cleanup() throws Exception {
        systemUserMap.clear();
        systemUserMap.putAll(originalSystemUserMap);
        authMap.clear();
        authMap.putAll(originalAuthMap);
        checkerMapperField.set(null, originalCheckerMapper);
        ApiAuthContext.release();
        if (UserContext.get() != null) {
            UserContext.get().release();
        }
        if (RequestContext.get() != null) {
            RequestContext.get().release();
        }
        TenantContext.get().release();
    }

    /** 无权限参数时展示稳定排序的全部注册身份，包含扩展身份但不访问凭据。 */
    @Test
    public void shouldListSafeLocalizedRegisteredUsersWithoutMemberQuery() {
        registerExtension();
        for (Locale locale : Arrays.asList(Locale.CHINESE, Locale.ENGLISH)) {
            RequestContext.init((RequestContext) null).setLocale(locale);
            JSONObject result = (JSONObject) listApi.myDoService(new JSONObject());
            JSONArray rows = result.getJSONArray("tbodyList");
            Assert.assertEquals(systemUserMap.size(), result.getInteger("rowNum").intValue());
            List<String> expected = SystemUserFactory.getSystemUserList().stream().map(ISystemUser::getUserUuid)
                    .collect(Collectors.toList());
            List<String> actual = new ArrayList<>();
            for (int i = 0; i < rows.size(); i++) {
                JSONObject row = rows.getJSONObject(i);
                Assert.assertEquals(Set.of("uuid", "userId", "userName"), row.keySet());
                actual.add(row.getString("uuid"));
                if ("extension-uuid".equals(row.getString("uuid"))) {
                    String name = "扩展系统用户";
                    if (Locale.ENGLISH.equals(locale)) {
                        name = "Extension user";
                    }
                    Assert.assertEquals(name, row.getString("userName"));
                }
            }
            Assert.assertEquals(expected, actual);
        }
        Assert.assertEquals(0, memberQueryCount);
    }

    /** 仅列出注册系统身份的直接成员，不混入普通用户、历史身份或只有继承权限的身份。 */
    @Test
    public void shouldQueryOnlyDirectRegisteredMembers() {
        grant("system", "USER_MODIFY");
        grant("ordinary-user", "USER_MODIFY");
        grant("unregistered-user", "USER_MODIFY");
        JSONObject request = new JSONObject();
        request.put("auth", "USER_MODIFY");
        JSONObject result = (JSONObject) listApi.myDoService(request);
        Assert.assertEquals(1, result.getInteger("rowNum").intValue());
        Assert.assertEquals("system", result.getJSONArray("tbodyList").getJSONObject(0).getString("uuid"));
        Assert.assertEquals(1, memberQueryCount);
    }

    /** 空工厂在带权限与不带权限时均返回空列表，不产生无约束授权查询。 */
    @Test
    public void shouldAvoidMemberQueryForEmptyRegistry() {
        systemUserMap.clear();
        JSONObject request = new JSONObject();
        request.put("auth", "USER_MODIFY");
        Assert.assertEquals(0, ((JSONObject) listApi.myDoService(request)).getInteger("rowNum").intValue());
        Assert.assertEquals(0, ((JSONObject) listApi.myDoService(new JSONObject())).getInteger("rowNum").intValue());
        Assert.assertEquals(0, memberQueryCount);
    }

    /** 所有系统操作的权限标识必须已注册，且校验失败不发生读写副作用。 */
    @Test
    public void shouldRejectUnknownAuthOnSystemBranch() throws Exception {
        JSONObject request = systemRequest("missing-auth", "system");
        for (PrivateApiComponentBase api : Arrays.asList(listApi, saveApi, deleteApi)) {
            try {
                api.myDoService(request);
                Assert.fail("未注册权限必须拒绝");
            } catch (AuthNotFoundException expected) {
                Assert.assertEquals(0, writeCount);
                Assert.assertEquals(0, memberQueryCount);
            }
        }
    }

    /** 新增成员采用追加与幂等写入，不覆盖其他权限、普通用户或未选中的系统成员。 */
    @Test
    public void shouldAppendIdempotentlyAndPreserveOtherMembersAndAuths() throws Exception {
        grant("system", "AUTHORITY_MODIFY");
        grant("anonymous", "USER_MODIFY");
        grant("ordinary-user", "USER_MODIFY");
        JSONObject request = systemRequest("USER_MODIFY", "system", "system");
        request.put("teamUuidList", array("test-team"));
        saveApi.myDoService(request);
        saveApi.myDoService(request);
        Assert.assertEquals(Set.of("AUTHORITY_MODIFY", "USER_MODIFY"), auths("system"));
        Assert.assertEquals(Set.of("USER_MODIFY"), auths("anonymous"));
        Assert.assertEquals(Set.of("USER_MODIFY"), auths("ordinary-user"));
        Assert.assertTrue(auths("team-member").isEmpty());
        Assert.assertEquals(0, expandUserCount);
    }

    /** 批量删除仅撤销当前权限，空集合无写入，也不影响未选成员及其他权限。 */
    @Test
    public void shouldDeleteSelectedCurrentAuthAndTreatEmptyBatchAsNoOp() throws Exception {
        for (String uuid : Arrays.asList("system", "anonymous", "autoexec", "ordinary-user")) {
            grant(uuid, "USER_MODIFY");
            grant(uuid, "AUTHORITY_MODIFY");
        }
        deleteApi.myDoService(systemRequest("USER_MODIFY", "system", "anonymous"));
        Assert.assertEquals(Set.of("AUTHORITY_MODIFY"), auths("system"));
        Assert.assertEquals(Set.of("AUTHORITY_MODIFY"), auths("anonymous"));
        Assert.assertTrue(auths("autoexec").contains("USER_MODIFY"));
        Assert.assertTrue(auths("ordinary-user").contains("USER_MODIFY"));
        int previousWrites = writeCount;
        deleteApi.myDoService(systemRequest("USER_MODIFY"));
        Assert.assertEquals(previousWrites, writeCount);
    }

    /** 系统 UUID 与用户 ID 不同的扩展身份只允许使用精确 UUID，并整批拒绝未知目标。 */
    @Test
    public void shouldValidateExactUuidForWholeBatchBeforeWriting() throws Exception {
        registerExtension();
        saveApi.myDoService(systemRequest("USER_MODIFY", "extension-uuid"));
        int previousWrites = writeCount;
        for (String invalidUuid : Arrays.asList("extension-id", "unknown-uuid", "ordinary-user")) {
            for (PrivateApiComponentBase api : Arrays.asList(saveApi, deleteApi)) {
                try {
                    api.myDoService(systemRequest("USER_MODIFY", "system", invalidUuid));
                    Assert.fail("整个批次必须通过系统 UUID 校验");
                } catch (UserNotFoundException expected) {
                    Assert.assertEquals(previousWrites, writeCount);
                    Assert.assertEquals(Set.of("USER_MODIFY"), auths("extension-uuid"));
                    Assert.assertTrue(auths("system").isEmpty());
                }
            }
        }
    }

    /** 新增及删除接口缺少 AUTHORITY_MODIFY 时在真实执行链拒绝，即使已有 USER_MODIFY。 */
    @Test
    public void shouldDenyAuthorityApisWithoutAuthorityModify() throws Exception {
        grant("ordinary-user", "USER_MODIFY");
        initUser("ordinary-user");
        for (PrivateApiComponentBase api : Arrays.asList(listApi, saveApi, deleteApi)) {
            try {
                api.doService(apiVo(api), systemRequest("USER_MODIFY", "system"), null);
                Assert.fail("没有 AUTHORITY_MODIFY 时必须拒绝权限管理接口");
            } catch (PermissionDeniedException expected) {
                Assert.assertEquals(0, writeCount);
                Assert.assertEquals(0, memberQueryCount);
            }
        }
    }

    /** 仅持有 AUTHORITY_MODIFY 即可完成系统成员查询、授权与撤权，不额外依赖 USER_MODIFY。 */
    @Test
    public void shouldAuthorizeWithAuthorityModifyOnly() throws Exception {
        grant("ordinary-user", "AUTHORITY_MODIFY");
        initUser("ordinary-user");
        JSONObject request = systemRequest("USER_MODIFY", "system");
        saveApi.doService(apiVo(saveApi), request, null);
        Assert.assertEquals(Set.of("USER_MODIFY"), auths("system"));
        JSONObject result = (JSONObject) listApi.doService(apiVo(listApi), request, null);
        Assert.assertEquals(1, result.getInteger("rowNum").intValue());
        deleteApi.doService(apiVo(deleteApi), request, null);
        Assert.assertTrue(auths("system").isEmpty());
        Assert.assertEquals(Set.of("AUTHORITY_MODIFY"), auths("ordinary-user"));
    }

    /** userType 的允许值由框架校验，大小写允许值仍进入系统分支而不会绕过目标校验。 */
    @Test
    public void shouldValidateUserTypeAndRespectAllowedCaseVariants() throws Exception {
        grant("ordinary-user", "AUTHORITY_MODIFY");
        initUser("ordinary-user");
        JSONObject request = systemRequest("USER_MODIFY", "system");
        request.put("userType", "SYSTEM");
        saveApi.doService(apiVo(saveApi), request, null);
        Assert.assertEquals(0, expandUserCount);
        request.put("userType", "other-type");
        int previousWrites = writeCount;
        for (PrivateApiComponentBase api : Arrays.asList(saveApi, deleteApi)) {
            try {
                api.doService(apiVo(api), request, null);
                Assert.fail("框架应拒绝未声明的 userType");
            } catch (ParamIrregularException expected) {
                Assert.assertEquals(previousWrites, writeCount);
            }
        }
    }

    /** 省略 userType 时继续展开普通用户及分组成员，普通删除行为保持原样。 */
    @Test
    public void shouldPreserveOrdinaryUserAndTeamPath() throws Exception {
        JSONObject request = systemRequest("USER_MODIFY", "ordinary-user");
        request.remove("userType");
        request.put("teamUuidList", array("test-team"));
        saveApi.myDoService(request);
        Assert.assertEquals(1, expandUserCount);
        Assert.assertEquals(Set.of("USER_MODIFY"), auths("ordinary-user"));
        Assert.assertEquals(Set.of("USER_MODIFY"), auths("team-member"));
        deleteApi.myDoService(request);
        Assert.assertTrue(auths("ordinary-user").isEmpty());
        Assert.assertEquals(Set.of("USER_MODIFY"), auths("team-member"));
    }

    /** 两个授权入口复用相同直接权限存储，权限管理添加后普通用户授权查询能回显，反向写入也能查到成员。 */
    @Test
    public void shouldShareStorageWithUserAuthorizationApis() throws Exception {
        saveApi.myDoService(systemRequest("USER_MODIFY", "system"));
        UserAuthSearchApi userSearchApi = new UserAuthSearchApi();
        inject(userSearchApi, "userMapper", mapper);
        inject(userSearchApi, "teamMapper", Proxy.newProxyInstance(TeamMapper.class.getClassLoader(),
                new Class<?>[]{TeamMapper.class}, (proxy, method, args) -> Collections.emptyList()));
        inject(userSearchApi, "roleMapper", Proxy.newProxyInstance(RoleMapper.class.getClassLoader(),
                new Class<?>[]{RoleMapper.class}, (proxy, method, args) -> Collections.emptyList()));
        JSONObject userQuery = new JSONObject();
        userQuery.put("userUuid", "system");
        JSONObject userResult = (JSONObject) userSearchApi.myDoService(userQuery);
        Assert.assertEquals("USER_MODIFY", userResult.getJSONObject("userAuthObj").getJSONArray("framework").getString(0));
        UserAuthSaveApi userSaveApi = new UserAuthSaveApi();
        inject(userSaveApi, "userMapper", mapper);
        JSONObject userSave = new JSONObject();
        userSave.put("userUuidList", array("system"));
        userSave.put("action", "cover");
        JSONObject authObj = new JSONObject();
        authObj.put("framework", array("AUTHORITY_MODIFY"));
        userSave.put("userAuthList", authObj);
        userSaveApi.myDoService(userSave);
        JSONObject filter = new JSONObject();
        filter.put("auth", "USER_MODIFY");
        Assert.assertEquals(0, ((JSONObject) listApi.myDoService(filter)).getInteger("rowNum").intValue());
        filter.put("auth", "AUTHORITY_MODIFY");
        Assert.assertEquals(1, ((JSONObject) listApi.myDoService(filter)).getInteger("rowNum").intValue());
    }

    /** 已有角色授权仍按当前权限覆盖角色成员，删除只撤销所选角色当前权限。 */
    @Test
    public void shouldKeepExistingRoleAuthorizationBehavior() throws Exception {
        Set<String> roleAuths = new HashSet<>(Arrays.asList("old-role#USER_MODIFY", "role-one#AUTHORITY_MODIFY"));
        RoleMapper roleMapper = (RoleMapper) Proxy.newProxyInstance(RoleMapper.class.getClassLoader(),
                new Class<?>[]{RoleMapper.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "deleteRoleAuthByAuth":
                            roleAuths.removeIf(key -> key.endsWith("#" + args[0]));
                            return 1;
                        case "checkRoleIsExists":
                            return 1;
                        case "insertRoleAuth":
                            RoleAuthVo added = (RoleAuthVo) args[0];
                            roleAuths.add(added.getRoleUuid() + "#" + added.getAuth());
                            return 1;
                        case "deleteRoleAuth":
                            RoleAuthVo removed = (RoleAuthVo) args[0];
                            roleAuths.remove(removed.getRoleUuid() + "#" + removed.getAuth());
                            return 1;
                        default:
                            throw new AssertionError("不应调用其他角色 Mapper 方法");
                    }
                });
        AuthRoleSaveApi roleSaveApi = new AuthRoleSaveApi();
        AuthRoleDeleteApi roleDeleteApi = new AuthRoleDeleteApi();
        inject(roleSaveApi, "roleMapper", roleMapper);
        inject(roleDeleteApi, "roleMapper", roleMapper);
        JSONObject request = new JSONObject();
        request.put("auth", "USER_MODIFY");
        request.put("authGroup", "framework");
        request.put("roleUuidList", array("role-one", "role-two"));
        roleSaveApi.myDoService(request);
        Assert.assertEquals(Set.of("role-one#AUTHORITY_MODIFY", "role-one#USER_MODIFY", "role-two#USER_MODIFY"), roleAuths);
        request.put("roleUuidList", array("role-one"));
        roleDeleteApi.myDoService(request);
        Assert.assertEquals(Set.of("role-one#AUTHORITY_MODIFY", "role-two#USER_MODIFY"), roleAuths);
    }

    /** 真实 Spring 注解事务拦截器在追加或删除后续成员失败时整批回滚。 */
    @Test
    public void shouldRollbackWholeSaveAndDeleteBatchOnWriteFailure() throws Exception {
        List<String> ids = new ArrayList<>(new HashSet<>(Arrays.asList("system", "anonymous")));
        for (String operation : Arrays.asList("insertUserAuth", "deleteUserAuth")) {
            authorityMap.clear();
            grant(ids.get(0), "AUTHORITY_MODIFY");
            grant(ids.get(1), "AUTHORITY_MODIFY");
            if ("deleteUserAuth".equals(operation)) {
                grant(ids.get(0), "USER_MODIFY");
                grant(ids.get(1), "USER_MODIFY");
            }
            Map<String, Map<String, String>> original = snapshot();
            failureMethod = operation;
            failureUuid = ids.get(1);
            int previousWrites = writeCount;
            int[] rollbacks = new int[1];
            PrivateApiComponentBase api = deleteApi;
            if ("insertUserAuth".equals(operation)) {
                api = saveApi;
            }
            PrivateApiComponentBase transactionalApi = transactional(api, rollbacks);
            try {
                transactionalApi.myDoService(systemRequest("USER_MODIFY", ids.toArray(new String[0])));
                Assert.fail("后续写入失败必须整批回滚");
            } catch (IllegalStateException expected) {
                Assert.assertEquals(original, authorityMap);
                Assert.assertTrue("失败前应已有目标发生写入", writeCount > previousWrites);
                Assert.assertEquals(1, rollbacks[0]);
            }
        }
    }

    /** 使用内存资源快照验证真实 Spring 事务注解的回滚路径，不连接数据库。 */
    private PrivateApiComponentBase transactional(PrivateApiComponentBase api, int[] rollbacks) {
        Map<String, Map<String, String>> before = snapshot();
        PlatformTransactionManager manager = new PlatformTransactionManager() {
            /** 建立测试事务。 */
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) {
                return new SimpleTransactionStatus();
            }

            /** 正常提交无需恢复内存资源。 */
            @Override
            public void commit(TransactionStatus status) {
            }

            /** 回滚时恢复批次前的直接权限快照。 */
            @Override
            public void rollback(TransactionStatus status) {
                authorityMap.clear();
                authorityMap.putAll(before);
                rollbacks[0]++;
            }
        };
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionManager(manager);
        interceptor.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        ProxyFactory factory = new ProxyFactory(api);
        factory.setProxyTargetClass(true);
        factory.addAdvice(interceptor);
        return (PrivateApiComponentBase) factory.getProxy();
    }

    /** 深复制内存存储，避免写入改变事务前快照。 */
    private Map<String, Map<String, String>> snapshot() {
        Map<String, Map<String, String>> result = new HashMap<>();
        authorityMap.forEach((uuid, values) -> result.put(uuid, new HashMap<>(values)));
        return result;
    }

    /** 注册动态扩展身份，任何凭据或 UserVo 构造调用均让测试失败。 */
    private void registerExtension() {
        ISystemUser user = (ISystemUser) Proxy.newProxyInstance(ISystemUser.class.getClassLoader(),
                new Class<?>[]{ISystemUser.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getUserId":
                            return "extension-id";
                        case "getUserUuid":
                            return "extension-uuid";
                        case "getUserName":
                            if (RequestContext.get() != null && Locale.ENGLISH.equals(RequestContext.get().getLocale())) {
                                return "Extension user";
                            }
                            return "扩展系统用户";
                        default:
                            throw new AssertionError("不应访问系统用户敏感方法：" + method.getName());
                    }
                });
        systemUserMap.put(user.getUserId(), user);
    }

    /** 建立未启用超级管理员的调用用户，接口授权只依赖数据库权限替身。 */
    private void initUser(String uuid) {
        UserVo user = new UserVo();
        user.setUuid(uuid);
        user.setUserId(uuid);
        user.setUserName(uuid);
        user.setIsSuperAdmin(false);
        user.setAuthorization("test-authorization");
        UserContext.init(user, new AuthenticationInfoVo(uuid), TimeUtil.ZONE_TIME);
    }

    /** 构造关闭审计的真实接口描述。 */
    private ApiVo apiVo(PrivateApiComponentBase api) {
        ApiVo result = new ApiVo();
        result.setToken(api.getToken());
        result.setModuleId("tenant");
        result.setNeedAudit(0);
        result.setIsActive(1);
        return result;
    }

    /** 创建用于系统成员维护的兼容请求。 */
    private JSONObject systemRequest(String auth, String... uuids) {
        JSONObject result = new JSONObject();
        result.put("auth", auth);
        result.put("authGroup", "framework");
        result.put("userType", "system");
        result.put("userUuidList", array(uuids));
        return result;
    }

    /** 将测试参数封装为实际 JSONArray。 */
    private JSONArray array(String... values) {
        return new JSONArray(new ArrayList<>(Arrays.asList(values)));
    }

    /** 向内存替身预置权限，不计入被测接口写入次数。 */
    private void grant(String uuid, String auth) {
        authorityMap.computeIfAbsent(uuid, key -> new HashMap<>()).put(auth, "framework");
    }

    /** 获取指定用户当前直接权限集合。 */
    private Set<String> auths(String uuid) {
        return new HashSet<>(authorityMap.getOrDefault(uuid, Collections.emptyMap()).keySet());
    }

    /** 将持久化替身投影为现有鉴权和权限查询使用的 Vo。 */
    private List<UserAuthVo> authVos(String uuid) {
        List<UserAuthVo> result = new ArrayList<>();
        authorityMap.getOrDefault(uuid, Collections.emptyMap()).forEach((auth, group) -> {
            UserAuthVo value = new UserAuthVo(uuid, auth);
            value.setAuthGroup(group);
            result.add(value);
        });
        return result;
    }

    /** 获取测试所需字段，生产代码无需提供测试装配入口。 */
    private Field field(Class<?> type, String name) throws Exception {
        Field result = type.getDeclaredField(name);
        result.setAccessible(true);
        return result;
    }

    /** 在测试中注入 Mapper 或服务替身。 */
    private void inject(Object target, String name, Object value) throws Exception {
        field(target.getClass(), name).set(target, value);
    }
}
