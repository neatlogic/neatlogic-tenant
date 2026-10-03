package neatlogic.module.tenant.api.user;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.RequestContext;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.ApiAuthContext;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.auth.core.AuthBase;
import neatlogic.framework.auth.core.AuthFactory;
import neatlogic.framework.auth.label.DATA_WAREHOUSE_BASE;
import neatlogic.framework.auth.label.DATA_WAREHOUSE_MODIFY;
import neatlogic.framework.auth.label.USER_MODIFY;
import neatlogic.framework.common.constvalue.systemuser.ISystemUser;
import neatlogic.framework.common.constvalue.systemuser.SystemUser;
import neatlogic.framework.common.constvalue.systemuser.SystemUserFactory;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.dto.RoleAuthVo;
import neatlogic.framework.dto.UserAuthVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.framework.util.TimeUtil;
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

/**
 * 用内存 Mapper 验证系统用户的安全列表、权限保存、查询与实际鉴权链，避免修改租户数据。
 */
public class SystemUserAuthorizationApiTest {
    private final Map<String, List<UserAuthVo>> directAuthMap = new HashMap<>();
    private final Set<String> ordinaryUserUuidSet = new HashSet<>();
    private final List<RoleAuthVo> roleAuthList = new ArrayList<>();
    private Map<String, ISystemUser> systemUserMap;
    private Map<String, ISystemUser> originalSystemUserMap;
    private Field checkerMapperField;
    private Object originalCheckerMapper;
    private UserAuthSaveApi saveApi;
    private UserAuthSearchApi searchApi;
    private int writeCount;
    private String failInsertUuid;

    /** 安装可记录授权读写的 Mapper，并保存全局注册表及鉴权依赖。 */
    @Before
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        Field registryField = SystemUserFactory.class.getDeclaredField("systemUserMap");
        registryField.setAccessible(true);
        systemUserMap = (Map<String, ISystemUser>) registryField.get(null);
        originalSystemUserMap = new HashMap<>(systemUserMap);
        ordinaryUserUuidSet.add("ordinary-user");
        UserMapper mapper = (UserMapper) Proxy.newProxyInstance(UserMapper.class.getClassLoader(),
                new Class[]{UserMapper.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "checkUserIsExists":
                            if (ordinaryUserUuidSet.contains(args[0])) {
                                return 1;
                            }
                            return 0;
                        case "searchUserAuthByUserUuid":
                            return new ArrayList<>(directAuthMap.getOrDefault(args[0], Collections.emptyList()));
                        case "searchUserRoleAuthByUserUuid":
                            return roleAuthList;
                        case "searchUserAllAuthByUserAuth":
                            String uuid = ((AuthenticationInfoVo) args[0]).getUserUuid();
                            return new ArrayList<>(directAuthMap.getOrDefault(uuid, Collections.emptyList()));
                        case "insertUserAuth":
                            UserAuthVo inserted = (UserAuthVo) args[0];
                            if (Objects.equals(failInsertUuid, inserted.getUserUuid())) {
                                throw new IllegalStateException("模拟后续用户的权限写入失败");
                            }
                            writeCount++;
                            List<UserAuthVo> list = directAuthMap.computeIfAbsent(inserted.getUserUuid(), key -> new ArrayList<>());
                            list.removeIf(auth -> Objects.equals(auth.getAuth(), inserted.getAuth()));
                            list.add(inserted);
                            return 1;
                        case "deleteUserAuth":
                            writeCount++;
                            UserAuthVo deleted = (UserAuthVo) args[0];
                            directAuthMap.computeIfAbsent(deleted.getUserUuid(), key -> new ArrayList<>())
                                    .removeIf(auth -> deleted.getAuth() == null || Objects.equals(auth.getAuth(), deleted.getAuth()));
                            return 1;
                        default:
                            throw new AssertionError("不应调用未配置的用户 Mapper 方法：" + method.getName());
                    }
                });
        saveApi = new UserAuthSaveApi();
        inject(saveApi, "userMapper", mapper);
        searchApi = new UserAuthSearchApi();
        inject(searchApi, "userMapper", mapper);
        inject(searchApi, "teamMapper", Proxy.newProxyInstance(TeamMapper.class.getClassLoader(),
                new Class[]{TeamMapper.class}, (proxy, method, args) -> Collections.emptyList()));
        inject(searchApi, "roleMapper", Proxy.newProxyInstance(RoleMapper.class.getClassLoader(),
                new Class[]{RoleMapper.class}, (proxy, method, args) -> Collections.emptyList()));
        checkerMapperField = AuthActionChecker.class.getDeclaredField("userMapper");
        checkerMapperField.setAccessible(true);
        originalCheckerMapper = checkerMapperField.get(null);
        checkerMapperField.set(null, mapper);
        TenantContext.init("test-tenant");
    }

    /** 恢复全局对象及线程上下文，避免影响其他回归测试。 */
    @After
    public void cleanup() throws Exception {
        systemUserMap.clear();
        systemUserMap.putAll(originalSystemUserMap);
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

    /** 注册扩展用户后列表完整展示，按请求语言读取名称且不触碰凭据或 JWT。 */
    @Test
    public void shouldListRegisteredUsersWithOnlySafeLocalizedFields() {
        registerDistinctIdUser();
        SearchSystemUserApi api = new SearchSystemUserApi();
        for (Locale locale : Arrays.asList(Locale.CHINESE, Locale.ENGLISH)) {
            RequestContext requestContext = RequestContext.init((RequestContext) null);
            requestContext.setLocale(locale);
            JSONArray rows = ((JSONObject) api.myDoService(new JSONObject())).getJSONArray("tbodyList");
            Assert.assertEquals(systemUserMap.size(), rows.size());
            Set<String> listed = new HashSet<>();
            for (int i = 0; i < rows.size(); i++) {
                JSONObject row = rows.getJSONObject(i);
                Assert.assertEquals(new HashSet<>(Arrays.asList("uuid", "userId", "userName")), row.keySet());
                listed.add(row.getString("uuid"));
                if ("extension-uuid".equals(row.getString("uuid"))) {
                    Assert.assertEquals("extension-id", row.getString("userId"));
                    if (Locale.ENGLISH.equals(locale)) {
                        Assert.assertEquals("Extension user", row.getString("userName"));
                    } else {
                        Assert.assertEquals("扩展系统用户", row.getString("userName"));
                    }
                }
            }
            for (ISystemUser user : systemUserMap.values()) {
                Assert.assertTrue(listed.contains(user.getUserUuid()));
            }
        }
    }

    /** 普通用户与无数据库记录的系统用户均保持追加、覆盖及指定权限删除语义。 */
    @Test
    public void shouldPreserveAddCoverAndDeleteForOrdinaryAndSystemUsers() throws Exception {
        for (String uuid : Arrays.asList("ordinary-user", SystemUser.SYSTEM.getUserUuid())) {
            saveApi.myDoService(request("cover", Collections.singletonList(uuid), "USER_MODIFY"));
            saveApi.myDoService(request("add", Collections.singletonList(uuid), "USER_MODIFY", "AUTHORITY_MODIFY"));
            Assert.assertEquals(new HashSet<>(Arrays.asList("USER_MODIFY", "AUTHORITY_MODIFY")), authNames(uuid));
            Assert.assertEquals(2, directAuthMap.get(uuid).size());
            saveApi.myDoService(request("delete", Collections.singletonList(uuid), "USER_MODIFY"));
            Assert.assertEquals(Collections.singleton("AUTHORITY_MODIFY"), authNames(uuid));
            saveApi.myDoService(request("cover", Collections.singletonList(uuid), "USER_MODIFY"));
            Assert.assertEquals(Collections.singleton("USER_MODIFY"), authNames(uuid));
        }
    }

    /** 空对象覆盖必须撤销全部直接权限，并能够通过现有查询接口回显。 */
    @Test
    public void shouldRoundTripAndClearAllDirectPermissions() throws Exception {
        String uuid = SystemUser.SYSTEM.getUserUuid();
        saveApi.myDoService(request("cover", Collections.singletonList(uuid), "USER_MODIFY"));
        JSONObject query = new JSONObject();
        query.put("userUuid", uuid);
        JSONObject granted = (JSONObject) searchApi.myDoService(query);
        Assert.assertEquals(Collections.singletonList("USER_MODIFY"),
                granted.getJSONObject("userAuthObj").getJSONArray("framework").toJavaList(String.class));
        saveApi.myDoService(request("cover", Collections.singletonList(uuid)));
        JSONObject revoked = (JSONObject) searchApi.myDoService(query);
        Assert.assertTrue(revoked.getJSONObject("userAuthObj").isEmpty());
        Assert.assertTrue(revoked.getJSONObject("userRoleAuthObj").isEmpty());
    }

    /** 查询保持角色继承权限单独显示的既有契约。 */
    @Test
    public void shouldKeepInheritedRolePermissionsInExistingResponse() throws Exception {
        String uuid = SystemUser.SYSTEM.getUserUuid();
        RoleAuthVo inherited = new RoleAuthVo();
        inherited.setAuth("AUTHORITY_MODIFY");
        inherited.setAuthGroup("framework");
        roleAuthList.add(inherited);
        saveApi.myDoService(request("cover", Collections.singletonList(uuid), "USER_MODIFY"));
        JSONObject query = new JSONObject();
        query.put("userUuid", uuid);
        JSONObject result = (JSONObject) searchApi.myDoService(query);
        Assert.assertEquals("USER_MODIFY", result.getJSONObject("userAuthObj").getJSONArray("framework").getString(0));
        Assert.assertEquals("AUTHORITY_MODIFY", result.getJSONObject("userRoleAuthObj").getJSONArray("framework").getString(0));
    }

    /** 注册用户 ID 与 UUID 不同时，仅 UUID 可以作为授权目标。 */
    @Test
    public void shouldAcceptRegisteredUuidAndRejectUserIdAlias() throws Exception {
        registerDistinctIdUser();
        saveApi.myDoService(request("cover", Collections.singletonList("extension-uuid"), "USER_MODIFY"));
        Assert.assertEquals(Collections.singleton("USER_MODIFY"), authNames("extension-uuid"));
        int previousWriteCount = writeCount;
        try {
            saveApi.myDoService(request("cover", Collections.singletonList("extension-id"), "USER_MODIFY"));
            Assert.fail("不允许把系统用户 ID 当作 UUID 授权");
        } catch (UserNotFoundException expected) {
            Assert.assertEquals(previousWriteCount, writeCount);
        }
    }

    /** 批量中存在未知目标时，普通用户及系统用户在每种操作下均不发生部分写入。 */
    @Test
    public void shouldValidateEveryTargetBeforeAnyWrite() throws Exception {
        for (String action : Arrays.asList("add", "cover", "delete")) {
            for (String uuid : Arrays.asList("ordinary-user", SystemUser.SYSTEM.getUserUuid())) {
                try {
                    saveApi.myDoService(request(action, Arrays.asList(uuid, "unknown-uuid"), "USER_MODIFY"));
                    Assert.fail("整批目标必须先完成存在性校验");
                } catch (UserNotFoundException expected) {
                    Assert.assertEquals(0, writeCount);
                    Assert.assertTrue(directAuthMap.isEmpty());
                }
            }
        }
    }

    /** 未豁免 API 中使用实时权限查询，授权后放行、撤权后立即拒绝。 */
    @Test
    public void shouldApplyGrantAndRevokeOnNextPermissionCheck() throws Exception {
        String uuid = SystemUser.SYSTEM.getUserUuid();
        initUser(uuid);
        ApiAuthContext.enter(SearchSystemUserApi.class);
        Assert.assertFalse(AuthActionChecker.check(USER_MODIFY.class));
        saveApi.myDoService(request("cover", Collections.singletonList(uuid), "USER_MODIFY"));
        Assert.assertTrue(AuthActionChecker.check(USER_MODIFY.class));
        saveApi.myDoService(request("cover", Collections.singletonList(uuid)));
        Assert.assertFalse(AuthActionChecker.check(USER_MODIFY.class));
    }

    /** 保存现有包含权限后，系统用户能够按既有穿透规则访问被包含权限。 */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldHonorIncludedPermissions() throws Exception {
        Field authMapField = AuthFactory.class.getDeclaredField("authMap");
        authMapField.setAccessible(true);
        Map<String, AuthBase> authMap = (Map<String, AuthBase>) authMapField.get(null);
        Map<String, AuthBase> originalAuthMap = new HashMap<>(authMap);
        try {
            authMap.put(DATA_WAREHOUSE_MODIFY.class.getSimpleName(), new DATA_WAREHOUSE_MODIFY());
            authMap.put(DATA_WAREHOUSE_BASE.class.getSimpleName(), new DATA_WAREHOUSE_BASE());
            String uuid = SystemUser.SYSTEM.getUserUuid();
            initUser(uuid);
            ApiAuthContext.enter(SearchSystemUserApi.class);
            Assert.assertFalse(AuthActionChecker.check(DATA_WAREHOUSE_BASE.class));
            saveApi.myDoService(request("cover", Collections.singletonList(uuid), "DATA_WAREHOUSE_MODIFY"));
            Assert.assertTrue(AuthActionChecker.check(DATA_WAREHOUSE_BASE.class));
            saveApi.myDoService(request("cover", Collections.singletonList(uuid)));
            Assert.assertFalse(AuthActionChecker.check(DATA_WAREHOUSE_BASE.class));
        } finally {
            authMap.clear();
            authMap.putAll(originalAuthMap);
        }
    }

    /** 普通用户及未豁免系统用户缺少 USER_MODIFY 时，真实执行入口拒绝列表和保存。 */
    @Test
    public void shouldDenyBothApiEntrypointsWithoutUserModify() throws Exception {
        for (String uuid : Arrays.asList("ordinary-user", SystemUser.SYSTEM.getUserUuid())) {
            initUser(uuid);
            assertDenied(new SearchSystemUserApi(), new JSONObject());
            assertDenied(saveApi, request("cover", Collections.singletonList(uuid), "USER_MODIFY"));
            Assert.assertEquals(0, writeCount);
        }
    }

    /** 已持有 USER_MODIFY 的调用者可以通过真实执行链查询系统用户列表。 */
    @Test
    public void shouldAllowAuthorizedListEntrypoint() throws Exception {
        String uuid = SystemUser.SYSTEM.getUserUuid();
        saveApi.myDoService(request("cover", Collections.singletonList(uuid), "USER_MODIFY"));
        initUser(uuid);
        SearchSystemUserApi api = new SearchSystemUserApi();
        JSONObject result = (JSONObject) api.doService(apiVo(api), new JSONObject(), null);
        Assert.assertEquals(systemUserMap.size(), result.getJSONArray("tbodyList").size());
        Assert.assertFalse(ApiAuthContext.isCurrentSystemUserExempt());
    }

    /** Spring 事务拦截器发现原有事务注解，后续写入异常会回滚整个批次。 */
    @Test
    public void shouldRollbackEarlierWritesWhenLaterInsertFails() throws Exception {
        final Map<String, List<UserAuthVo>> snapshot = new HashMap<>();
        final int[] transactionCounts = new int[2];
        PlatformTransactionManager manager = new PlatformTransactionManager() {
            /** 事务开始时保存内存持久化资源快照。 */
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) {
                directAuthMap.forEach((uuid, list) -> snapshot.put(uuid, new ArrayList<>(list)));
                return new SimpleTransactionStatus();
            }

            /** 记录正常提交。 */
            @Override
            public void commit(TransactionStatus status) {
                transactionCounts[0]++;
            }

            /** 模拟事务资源回滚，保留批量操作前的已有授权。 */
            @Override
            public void rollback(TransactionStatus status) {
                directAuthMap.clear();
                directAuthMap.putAll(snapshot);
                transactionCounts[1]++;
            }
        };
        saveApi.myDoService(request("cover", Collections.singletonList("ordinary-user"), "AUTHORITY_MODIFY"));
        failInsertUuid = SystemUser.SYSTEM.getUserUuid();
        ProxyFactory factory = new ProxyFactory(saveApi);
        factory.setProxyTargetClass(true);
        factory.addAdvice(new TransactionInterceptor(manager, new AnnotationTransactionAttributeSource()));
        UserAuthSaveApi transactionalApi = (UserAuthSaveApi) factory.getProxy();
        try {
            transactionalApi.myDoService(request("cover", Arrays.asList("ordinary-user", failInsertUuid), "USER_MODIFY"));
            Assert.fail("后续写入失败必须传播异常并回滚");
        } catch (IllegalStateException expected) {
            Assert.assertEquals(Collections.singleton("AUTHORITY_MODIFY"), authNames("ordinary-user"));
            Assert.assertFalse(directAuthMap.containsKey(failInsertUuid));
            Assert.assertEquals(0, transactionCounts[0]);
            Assert.assertEquals(1, transactionCounts[1]);
        }
    }

    /** 通过动态代理扩展注册表，避免增加会被 Reflections 扫描的测试实现类。 */
    private void registerDistinctIdUser() {
        ISystemUser extension = (ISystemUser) Proxy.newProxyInstance(ISystemUser.class.getClassLoader(),
                new Class[]{ISystemUser.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getUserUuid":
                            return "extension-uuid";
                        case "getUserId":
                            return "extension-id";
                        case "getUserName":
                            if (RequestContext.get() != null && Locale.ENGLISH.equals(RequestContext.get().getLocale())) {
                                return "Extension user";
                            }
                            return "扩展系统用户";
                        default:
                            throw new AssertionError("列表与保存不得读取系统用户敏感信息：" + method.getName());
                    }
                });
        systemUserMap.put("extension-id", extension);
    }

    /** 在测试中直接注入替身，生产代码保持标准字段注入。 */
    private void inject(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    /** 构造沿用普通用户协议的授权保存请求；无权限时传空对象覆盖。 */
    private JSONObject request(String action, List<String> uuids, String... auths) {
        JSONObject request = new JSONObject();
        request.put("action", action);
        request.put("userUuidList", new JSONArray(new ArrayList<>(uuids)));
        JSONObject authObj = new JSONObject();
        if (auths.length > 0) {
            authObj.put("framework", new JSONArray(new ArrayList<>(Arrays.asList(auths))));
        }
        request.put("userAuthList", authObj);
        return request;
    }

    /** 读取内存持久化资源中的权限标识。 */
    private Set<String> authNames(String uuid) {
        Set<String> names = new HashSet<>();
        for (UserAuthVo auth : directAuthMap.getOrDefault(uuid, Collections.emptyList())) {
            names.add(auth.getAuth());
        }
        return names;
    }

    /** 建立非超级管理员用户上下文，验证真实权限判断。 */
    private void initUser(String uuid) {
        UserVo user = new UserVo();
        user.setUuid(uuid);
        user.setUserId(uuid);
        user.setUserName(uuid);
        user.setAuthorization("test-authorization");
        user.setIsSuperAdmin(false);
        UserContext.init(user, new AuthenticationInfoVo(uuid), TimeUtil.ZONE_TIME);
    }

    /** 构造关闭审计的接口描述，避免测试访问真实存储。 */
    private ApiVo apiVo(PrivateApiComponentBase api) {
        ApiVo apiVo = new ApiVo();
        apiVo.setToken(api.getToken());
        apiVo.setIsActive(1);
        apiVo.setModuleId("tenant");
        apiVo.setNeedAudit(0);
        return apiVo;
    }

    /** 调用公共执行入口并确认拒绝后无鉴权作用域残留。 */
    private void assertDenied(PrivateApiComponentBase api, JSONObject request) throws Exception {
        try {
            api.doService(apiVo(api), request, null);
            Assert.fail("缺少 USER_MODIFY 时接口应拒绝执行");
        } catch (PermissionDeniedException expected) {
            Assert.assertFalse(ApiAuthContext.isCurrentSystemUserExempt());
        }
    }
}
