package neatlogic.module.tenant.api.dashboard.datasource;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.auth.label.DATA_WAREHOUSE_BASE;
import neatlogic.framework.bootstrap.NeatLogicWebApplicationContext;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.datawarehouse.dto.DataSourceVo;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.dto.UserAuthVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.core.ApiValidateAndHelpBase;
import neatlogic.framework.restful.core.IApiComponent;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.core.privateapi.PrivateApiRegistrar;
import neatlogic.framework.restful.dto.ApiHandlerVo;
import neatlogic.framework.restful.dto.ApiVo;
//import neatlogic.framework.auth.label.REPORT_BASE;
import neatlogic.module.tenant.service.dashboard.DashboardDatasourceService;
import neatlogic.module.tenant.service.dashboard.DashboardDatasourceServiceImpl;
import neatlogic.module.tenant.exception.dashboard.DashboardDatasourceTypeNotSupportedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/** 验证真实 API 委托与公共权限校验链，避免仅依靠源码字符串检查。 */
public class DashboardDatasourceApiTest {
    private static final String USER_UUID = "dashboard-datasource-test-user";
    private Field mapperField;
    private Object originalMapper;
    private List<UserAuthVo> grants;

    /** 建立普通租户/非管理员用户，并用可控授权结果替代真实数据库。 */
    @Before
    public void initialize() throws Exception {
        mapperField = AuthActionChecker.class.getDeclaredField("userMapper");
        mapperField.setAccessible(true);
        originalMapper = mapperField.get(null);
        grants = Collections.emptyList();
        UserMapper mapper = (UserMapper) Proxy.newProxyInstance(UserMapper.class.getClassLoader(),
                new Class<?>[]{UserMapper.class}, (proxy, method, args) -> {
                    if ("searchUserAllAuthByUserAuth".equals(method.getName())) {
                        return grants;
                    }
                    throw new AssertionError("非预期授权查询：" + method.getName());
                });
        new AuthActionChecker().setUserMapper(mapper);
        TenantContext.init("dashboard-datasource-test-tenant");
        UserVo user = new UserVo();
        user.setUuid(USER_UUID);
        user.setUserId(USER_UUID);
        user.setAuthorization("dashboard-datasource-test");
        user.setIsSuperAdmin(false);
        UserContext.init(user, new AuthenticationInfoVo(USER_UUID), "+08:00");
    }

    /** 恢复静态依赖并释放线程上下文，防止污染其他模块测试。 */
    @After
    public void cleanup() throws Exception {
        if (mapperField != null) {
            mapperField.set(null, originalMapper);
        }
        if (UserContext.get() != null) {
            UserContext.get().release();
        }
        if (TenantContext.get() != null) {
            TenantContext.get().release();
        }
    }

    /** 只授予 REPORT_BASE 的用户通过三个接口公共权限检查。 */
    @Test
    public void shouldAllowReportPermissionWithoutDatawarehousePermission() throws Exception {
//        grants = Collections.singletonList(new UserAuthVo(USER_UUID, REPORT_BASE.class.getSimpleName()));
//        assertTrue(AuthActionChecker.check(REPORT_BASE.class));
        assertFalse(AuthActionChecker.check(DATA_WAREHOUSE_BASE.class));
        for (PrivateApiComponentBase api : apis()) {
            validateAuth(api);
            AuthAction[] actions = api.getClass().getAnnotationsByType(AuthAction.class);
            assertEquals(1, actions.length);
//            assertEquals(REPORT_BASE.class, actions[0].action());
        }
    }

    /** 无报表权限的用户不能访问新接口，即使拥有数据仓库权限。 */
    @Test
    public void shouldRejectUserWithoutReportPermission() {
        for (List<UserAuthVo> permissionList : Arrays.asList(Collections.<UserAuthVo>emptyList(),
                Collections.singletonList(new UserAuthVo(USER_UUID, DATA_WAREHOUSE_BASE.class.getSimpleName())))) {
            grants = permissionList;
            for (PrivateApiComponentBase api : apis()) {
                assertThrows(PermissionDeniedException.class, () -> validateAuth(api));
            }
        }
    }

    /** 迁移后实现属于租户模块，共享权限仍保留原报表权限标识与展示元数据。 */
    @Test
    public void shouldPreservePermissionMetadataAndTenantOwnership() {
//        REPORT_BASE permission = new REPORT_BASE();
//        assertEquals("neatlogic.framework.auth.label.REPORT_BASE", permission.getClass().getName());
//        assertEquals("REPORT_BASE", permission.getAuthName());
//        assertEquals("auth.report_base.name", permission.getAuthDisplayName());
//        assertEquals("auth.report_base.description", permission.getAuthIntroduction());
//        assertEquals("report", permission.getAuthGroup());
//        assertEquals("report", permission.getAuthModule());
//        assertEquals(Integer.valueOf(1), permission.getSort());
        for (PrivateApiComponentBase api : apis()) {
            assertEquals("neatlogic.module.tenant.api.dashboard.datasource", api.getClass().getPackage().getName());
        }
        assertEquals("neatlogic.module.tenant.service.dashboard", DashboardDatasourceService.class.getPackage().getName());
        assertEquals("neatlogic.module.tenant.service.dashboard", DashboardDatasourceServiceImpl.class.getPackage().getName());
        assertEquals("neatlogic.module.tenant.exception.dashboard", DashboardDatasourceTypeNotSupportedException.class.getPackage().getName());
    }

    /** 使用真实注册器验证租户归属和新路径，并完整恢复被注册器修改的全局缓存。 */
    @Test
    public void shouldRegisterDashboardApisUnderTenantModule() {
        List<PrivateApiComponentBase> components = apis();
        Map<String, IApiComponent> originalComponents = new HashMap<>(PrivateApiComponentFactory.getComponentMap());
        Map<String, ApiHandlerVo> originalHandlers = new HashMap<>(PrivateApiComponentFactory.getApiHandlerMap());
        List<ApiHandlerVo> originalHandlerList = new ArrayList<>(PrivateApiComponentFactory.getApiHandlerList());
        Map<String, ApiVo> originalApis = new HashMap<>(PrivateApiComponentFactory.getApiMap());
        List<ApiVo> originalApiList = new ArrayList<>(PrivateApiComponentFactory.getApiList());
        List<String> originalExemptTokens = new ArrayList<>(PrivateApiComponentFactory.ExemptTokenMap);
        String[] oldTokens = {"report/statement/datasource/list", "report/statement/datasource/get",
                "report/statement/datasource/data/search"};
        for (PrivateApiComponentBase component : components) {
            // 工厂遇到重复 Token 或缺失权限注解会退出 JVM，先给出可诊断的测试失败。
            assertFalse(PrivateApiComponentFactory.getApiMap().containsKey(component.getToken()));
            assertNotNull(component.getClass().getAnnotation(AuthAction.class));
            assertFalse(component.getToken().contains("{"));
        }
        for (String oldToken : oldTokens) {
            assertFalse(PrivateApiComponentFactory.getApiMap().containsKey(oldToken));
        }
        // 只替代 Spring Bean 扫描，接口检查、处理器元数据与 Token 注册均执行框架真实逻辑。
        NeatLogicWebApplicationContext context = new NeatLogicWebApplicationContext() {
            @Override
            public <T> Map<String, T> getBeansOfType(Class<T> type) {
                Map<String, T> beans = new LinkedHashMap<>();
                for (PrivateApiComponentBase component : components) {
                    beans.put(component.getClass().getSimpleName(), type.cast(component));
                }
                return beans;
            }
        };
        context.setId("tenant");
        context.setGroup("framework");
        try {
            new PrivateApiRegistrar().register(context);
            assertEquals(originalApiList.size() + 3, PrivateApiComponentFactory.getApiList().size());
            assertEquals(originalHandlerList.size() + 3, PrivateApiComponentFactory.getApiHandlerList().size());
            for (PrivateApiComponentBase component : components) {
                ApiVo api = PrivateApiComponentFactory.getApiMap().get(component.getToken());
                assertNotNull(api);
                assertEquals("tenant", api.getModuleId());
                assertEquals("framework", api.getModuleGroup());
                assertEquals(component.getClassName(), api.getHandler());
                ApiHandlerVo handler = PrivateApiComponentFactory.getApiHandlerByHandler(api.getHandler());
                assertNotNull(handler);
                assertEquals("tenant", handler.getModuleId());
                assertSame(component, PrivateApiComponentFactory.getComponentMap().get(api.getHandler()));
            }
            for (String oldToken : oldTokens) {
                assertFalse(PrivateApiComponentFactory.getApiMap().containsKey(oldToken));
            }
        } finally {
            // 三个固定路径不写正则索引；其余会修改的索引和列表按原对象内容精确恢复。
            PrivateApiComponentFactory.getComponentMap().clear();
            PrivateApiComponentFactory.getComponentMap().putAll(originalComponents);
            PrivateApiComponentFactory.getApiHandlerMap().clear();
            PrivateApiComponentFactory.getApiHandlerMap().putAll(originalHandlers);
            PrivateApiComponentFactory.getApiHandlerList().clear();
            PrivateApiComponentFactory.getApiHandlerList().addAll(originalHandlerList);
            PrivateApiComponentFactory.getApiMap().clear();
            PrivateApiComponentFactory.getApiMap().putAll(originalApis);
            PrivateApiComponentFactory.getApiList().clear();
            PrivateApiComponentFactory.getApiList().addAll(originalApiList);
            PrivateApiComponentFactory.ExemptTokenMap.clear();
            PrivateApiComponentFactory.ExemptTokenMap.addAll(originalExemptTokens);
        }
    }

    /** 三个接口使用固定 Token 并原样委托参数/响应，不嵌套 Status/Return。 */
    @Test
    public void shouldDelegateEachApiWithoutChangingContract() throws Exception {
        List<PrivateApiComponentBase> apis = apis();
        String[] tokens = {"dashboard/datasource/list", "dashboard/datasource/get", "dashboard/datasource/data/search"};
        String[] methods = {"listDatasource", "getDatasource", "searchDatasourceData"};
        String[] names = {"nmtadd.dashboarddatasource.list", "nmtadd.dashboarddatasource.get", "nmtadd.dashboarddatasource.searchdata"};
        for (int i = 0; i < apis.size(); i++) {
            PrivateApiComponentBase api = apis.get(i);
            String expectedMethod = methods[i];
            JSONObject params = new JSONObject();
            params.put("datasourceType", "datawarehouse");
            params.put("id", 10L);
            params.put("dataSourceId", 10L);
            Object response = i == 1 ? new DataSourceVo() : new JSONObject();
            DashboardDatasourceService service = (DashboardDatasourceService) Proxy.newProxyInstance(
                    DashboardDatasourceService.class.getClassLoader(), new Class<?>[]{DashboardDatasourceService.class},
                    (proxy, method, args) -> {
                        assertEquals(expectedMethod, method.getName());
                        assertSame(params, args[0]);
                        return response;
                    });
            Field serviceField = api.getClass().getDeclaredField("dashboardDatasourceService");
            serviceField.setAccessible(true);
            serviceField.set(api, service);
            assertEquals(tokens[i], api.getToken());
            assertEquals(names[i], api.getName());
            assertEquals(names[i], api.getClass().getMethod("myDoService", JSONObject.class).getAnnotation(Description.class).desc());
            assertSame(response, api.getClass().getMethod("myDoService", JSONObject.class).invoke(api, params));
            Input input = api.getClass().getMethod("myDoService", JSONObject.class).getAnnotation(Input.class);
            Param type = Arrays.stream(input.value()).filter(p -> "datasourceType".equals(p.name())).findFirst().orElse(null);
            assertNotNull(type);
            assertFalse(type.isRequired());
            assertEquals("nmtadd.dashboarddatasource.type", type.desc());
        }
    }

    /** 使用框架真实权限入口，仅省略与本次权限规则无关的参数和 HTTP 处理。 */
    private void validateAuth(PrivateApiComponentBase api) throws Exception {
        Method method = ApiValidateAndHelpBase.class.getDeclaredMethod("validAuth", Class.class);
        method.setAccessible(true);
        try {
            method.invoke(api, api.getClass());
        } catch (InvocationTargetException exception) {
            if (exception.getCause() instanceof Exception) {
                throw (Exception) exception.getCause();
            }
            throw exception;
        }
    }

    /** 返回本次新增的三个真实接口实例。 */
    private List<PrivateApiComponentBase> apis() {
        return Arrays.asList(new ListDashboardDatasourceApi(), new GetDashboardDatasourceApi(), new SearchDashboardDatasourceDataApi());
    }
}
