package neatlogic.module.tenant.api.dashboard.datasource;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.auth.label.DATA_WAREHOUSE_BASE;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.bootstrap.NeatLogicWebApplicationContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dashboard.datasource.core.DashboardDatasourceHandlerFactory;
import neatlogic.framework.dashboard.datasource.core.IDashboardDatasourceHandler;
import neatlogic.framework.dashboard.datasource.exception.DashboardDatasourceTypeNotSupportedException;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.dto.UserAuthVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.annotation.NotDefined;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.ApiValidateAndHelpBase;
import neatlogic.framework.restful.core.IApiComponent;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.core.privateapi.PrivateApiRegistrar;
import neatlogic.framework.restful.dto.ApiHandlerVo;
import neatlogic.framework.restful.dto.ApiVo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.event.ContextRefreshedEvent;

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

/** 验证真实模块注册、API 类型分发和当前 NoAuth 契约，不依赖数据库或 HTTP。 */
public class DashboardDatasourceApiTest {
    private static final String USER_UUID = "dashboard-datasource-test-user";
    private static final String[] OPERATIONS = {"listDatasource", "getDatasource", "searchDatasourceData"};
    private Field mapperField;
    private Object originalMapper;
    private List<UserAuthVo> grants;
    private Map<String, IDashboardDatasourceHandler> handlers;
    private Map<String, IDashboardDatasourceHandler> originalHandlers;
    private RecordingHandler datawarehouseHandler;
    private RecordingHandler extensionHandler;

    /** 隔离静态工厂并通过真实模块刷新事件注册两个测试 Handler，普通用户默认不授予功能权限。 */
    @Before
    @SuppressWarnings("unchecked")
    public void initialize() throws Exception {
        Field handlerField = DashboardDatasourceHandlerFactory.class.getDeclaredField("handlerMap");
        handlerField.setAccessible(true);
        handlers = (Map<String, IDashboardDatasourceHandler>) handlerField.get(null);
        originalHandlers = new HashMap<>(handlers);
        handlers.clear();
        datawarehouseHandler = new RecordingHandler("datawarehouse");
        extensionHandler = new RecordingHandler("test-extension");
        registerHandler("tenant", datawarehouseHandler);
        registerHandler("test-extension-module", extensionHandler);
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
        if (handlers != null && originalHandlers != null) {
            handlers.clear();
            handlers.putAll(originalHandlers);
        }
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

    /** 三个接口保留 NoAuth，普通用户是否具有数据仓库权限均不影响功能权限检查。 */
    @Test
    public void shouldPreserveNoAuthContract() throws Exception {
        assertFalse(AuthActionChecker.check(DATA_WAREHOUSE_BASE.class));
        for (List<UserAuthVo> permissionList : Arrays.asList(Collections.<UserAuthVo>emptyList(),
                Collections.singletonList(new UserAuthVo(USER_UUID, DATA_WAREHOUSE_BASE.class.getSimpleName())))) {
            grants = permissionList;
            for (PrivateApiComponentBase api : apis()) {
                validateAuth(api);
                AuthAction[] actions = api.getClass().getAnnotationsByType(AuthAction.class);
                assertEquals(1, actions.length);
                assertEquals(NoAuth.class, actions[0].action());
            }
        }
    }

    /** 保留路径和类型参数元数据，详情仅声明通用 JSON 字段列表，避免泄露数据仓库 DTO。 */
    @Test
    public void shouldPreserveApiMetadataAndTenantOwnership() throws Exception {
        String[] tokens = {"dashboard/datasource/list", "dashboard/datasource/get", "dashboard/datasource/data/search"};
        String[] names = {"nmtadd.dashboarddatasource.list", "nmtadd.dashboarddatasource.get", "nmtadd.dashboarddatasource.searchdata"};
        List<PrivateApiComponentBase> components = apis();
        for (int i = 0; i < components.size(); i++) {
            PrivateApiComponentBase api = components.get(i);
            assertEquals("neatlogic.module.tenant.api.dashboard.datasource", api.getClass().getPackage().getName());
            assertEquals(tokens[i], api.getToken());
            assertEquals(names[i], api.getName());
            Method method = api.getClass().getMethod("myDoService", JSONObject.class);
            assertEquals(names[i], method.getAnnotation(Description.class).desc());
            Param type = Arrays.stream(method.getAnnotation(Input.class).value())
                    .filter(param -> "datasourceType".equals(param.name())).findFirst().orElse(null);
            assertNotNull(type);
            assertFalse(type.isRequired());
            assertEquals("nmtadd.dashboarddatasource.type", type.desc());
        }
        // 详情协议只声明 fieldList，不通过 explode 展开底层数据源或字段 DTO。
        Method detailMethod = GetDashboardDatasourceApi.class.getMethod("myDoService", JSONObject.class);
        assertEquals(JSONObject.class, detailMethod.getReturnType());
        Output detailOutput = detailMethod.getAnnotation(Output.class);
        assertNotNull(detailOutput);
        assertEquals(1, detailOutput.value().length);
        Param fieldList = detailOutput.value()[0];
        assertEquals("fieldList", fieldList.name());
        assertEquals(ApiParamType.JSONARRAY, fieldList.type());
        assertEquals("nmtadd.dashboarddatasource.fieldlist", fieldList.desc());
        assertEquals("nmtadd.dashboarddatasource.fieldlisthelp", fieldList.help());
        assertEquals(NotDefined.class, fieldList.explode());
        // 列表同样只声明通用 JSON 选项，不把数值 ID DTO 作为集成 UUID 的输出契约。
        Method listMethod = ListDashboardDatasourceApi.class.getMethod("myDoService", JSONObject.class);
        Param options = Arrays.stream(listMethod.getAnnotation(Output.class).value())
                .filter(param -> "tbodyList".equals(param.name())).findFirst().orElse(null);
        assertNotNull(options);
        assertEquals(ApiParamType.JSONARRAY, options.type());
        assertEquals("nmtadd.dashboarddatasource.tbodylist", options.desc());
        assertEquals("nmtadd.dashboarddatasource.tbodylisthelp", options.help());
        assertEquals(NotDefined.class, options.explode());
    }

    /** 使用真实注册器验证租户归属和新路径，并完整恢复被注册器修改的全局缓存。 */
    @Test
    public void shouldRegisterDashboardApisUnderTenantModule() {
        List<PrivateApiComponentBase> components = new ArrayList<>(apis());
        components.add(new ListDashboardDatasourceTypeApi());
        Map<String, IApiComponent> originalComponents = new HashMap<>(PrivateApiComponentFactory.getComponentMap());
        Map<String, ApiHandlerVo> originalApiHandlers = new HashMap<>(PrivateApiComponentFactory.getApiHandlerMap());
        List<ApiHandlerVo> originalHandlerList = new ArrayList<>(PrivateApiComponentFactory.getApiHandlerList());
        Map<String, ApiVo> originalApis = new HashMap<>(PrivateApiComponentFactory.getApiMap());
        List<ApiVo> originalApiList = new ArrayList<>(PrivateApiComponentFactory.getApiList());
        List<String> originalExemptTokens = new ArrayList<>(PrivateApiComponentFactory.ExemptTokenMap);
        String[] oldTokens = {"report/statement/datasource/list", "report/statement/datasource/get",
                "report/statement/datasource/data/search"};
        for (PrivateApiComponentBase component : components) {
            // 工厂遇到缺失权限注解会退出 JVM，先给出可诊断的测试失败。
            assertNotNull(component.getClass().getAnnotation(AuthAction.class));
            assertFalse(component.getToken().contains("{"));
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
            // 隔离既有注册，避免与其他测试的同名 Token 冲突；finally 恢复原内容。
            PrivateApiComponentFactory.getComponentMap().clear();
            PrivateApiComponentFactory.getApiHandlerMap().clear();
            PrivateApiComponentFactory.getApiHandlerList().clear();
            PrivateApiComponentFactory.getApiMap().clear();
            PrivateApiComponentFactory.getApiList().clear();
            PrivateApiComponentFactory.ExemptTokenMap.clear();
            new PrivateApiRegistrar().register(context);
            assertEquals(4, PrivateApiComponentFactory.getApiList().size());
            assertEquals(4, PrivateApiComponentFactory.getApiHandlerList().size());
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
            // 四个固定路径不写正则索引；其余会修改的索引和列表按原对象内容精确恢复。
            PrivateApiComponentFactory.getComponentMap().clear();
            PrivateApiComponentFactory.getComponentMap().putAll(originalComponents);
            PrivateApiComponentFactory.getApiHandlerMap().clear();
            PrivateApiComponentFactory.getApiHandlerMap().putAll(originalApiHandlers);
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

    /** 两种已注册类型分别处理三个操作，原始 JSONObject 与返回对象均不复制、不包装。 */
    @Test
    public void shouldDispatchEachApiToRegisteredHandler() throws Exception {
        List<PrivateApiComponentBase> components = apis();
        for (RecordingHandler handler : Arrays.asList(datawarehouseHandler, extensionHandler)) {
            RecordingHandler other = handler == datawarehouseHandler ? extensionHandler : datawarehouseHandler;
            int otherCalls = other.calls;
            for (int i = 0; i < components.size(); i++) {
                JSONObject params = requestParams();
                params.put("datasourceType", handler.getType());
                JSONObject before = JSONObject.parseObject(params.toJSONString());
                int calls = handler.calls;
                assertSame(handler.response(i), invokeApi(components.get(i), params));
                assertSame(params, handler.lastParams);
                assertEquals(before, params);
                assertEquals(OPERATIONS[i], handler.lastOperation);
                assertEquals(calls + 1, handler.calls);
                assertEquals(otherCalls, other.calls);
            }
        }
    }

    /** 真实输入校验同时接收 UUID、数字和数字字符串，再将原 JSONObject 交给对应 Handler。 */
    @Test
    public void shouldAcceptUuidAndNumericDatasourceIds() throws Exception {
        RecordingHandler integration = new RecordingHandler("integration");
        registerHandler("framework", integration);
        String uuid = "91f50f88-cc4a-42f3-8538-bcb57be0302c";
        for (Object id : new Object[]{uuid, 101L, "101"}) {
            RecordingHandler handler = uuid.equals(id) ? integration : datawarehouseHandler;
            for (int i = 1; i < 3; i++) {
                PrivateApiComponentBase api = apis().get(i);
                String parameter = i == 1 ? "id" : "dataSourceId";
                JSONObject params = new JSONObject();
                params.put("datasourceType", handler.getType());
                params.put(parameter, id);
                Method method = api.getClass().getMethod("myDoService", JSONObject.class);
                Param idParam = Arrays.stream(method.getAnnotation(Input.class).value())
                        .filter(param -> parameter.equals(param.name())).findFirst().orElse(null);
                assertNotNull(idParam);
                assertEquals(ApiParamType.STRING, idParam.type());
                assertTrue(idParam.isRequired());
                // 执行真实框架 STRING 校验；旧 JSON 数字也能规范为字符串，原 Handler 的 getLong 仍可读取。
                api.validInput(method, params);
                assertEquals(String.valueOf(id), params.get(parameter));
                assertSame(handler.response(i), invokeApi(api, params));
                assertSame(params, handler.lastParams);
                assertEquals(OPERATIONS[i], handler.lastOperation);
                if (handler == datawarehouseHandler) {
                    assertEquals(Long.valueOf(101L), handler.lastParams.getLong(parameter));
                }
            }
        }
        assertEquals(2, integration.calls);
        assertEquals(4, datawarehouseHandler.calls);
        assertEquals(0, extensionHandler.calls);
    }

    /** 显式数据仓库类型按原参数分发，API 不回写或改写请求。 */
    @Test
    public void shouldPreserveExplicitDatawarehouseTypeForEachApi() throws Exception {
        List<PrivateApiComponentBase> components = apis();
        for (int i = 0; i < components.size(); i++) {
            JSONObject params = requestParams();
            params.put("datasourceType", "datawarehouse");
            JSONObject before = JSONObject.parseObject(params.toJSONString());
            assertSame(datawarehouseHandler.response(i), invokeApi(components.get(i), params));
            assertSame(params, datawarehouseHandler.lastParams);
            assertEquals(OPERATIONS[i], datawarehouseHandler.lastOperation);
            assertEquals(before, params);
        }
        assertEquals(3, datawarehouseHandler.calls);
        assertEquals(0, extensionHandler.calls);
    }

    /** 空字符串、空白及未注册的显式类型直接报错，任何 Handler 都不得收到查询。 */
    @Test
    public void shouldRejectUnsupportedTypesBeforeQuerying() {
        for (String type : new String[]{"", " \t\r\n", "integration", "unknown", " datawarehouse ", "DATAWAREHOUSE"}) {
            for (PrivateApiComponentBase api : apis()) {
                JSONObject params = requestParams();
                params.put("datasourceType", type);
                assertThrows(DashboardDatasourceTypeNotSupportedException.class, () -> invokeApi(api, params));
                assertEquals(type, params.getString("datasourceType"));
            }
        }
        assertEquals(0, datawarehouseHandler.calls);
        assertEquals(0, extensionHandler.calls);
    }

    /** 类型列表无须指定来源，严格返回 tbodyList 与 value/text，不调用数据源的任何查询方法。 */
    @Test
    public void shouldListDatasourceTypesWithoutQueryingSources() {
        RecordingHandler integration = new RecordingHandler("integration");
        registerHandler("framework", integration);
        JSONObject params = new JSONObject().fluentPut("pageSize", 100);
        JSONObject before = JSONObject.parseObject(params.toJSONString());
        JSONObject result = new ListDashboardDatasourceTypeApi().myDoService(params);
        assertEquals(Collections.singleton("tbodyList"), result.keySet());
        JSONArray options = result.getJSONArray("tbodyList");
        assertEquals(3, options.size());
        assertEquals(Arrays.asList("datawarehouse", "integration", "test-extension"),
                Arrays.asList(options.getJSONObject(0).getString("value"), options.getJSONObject(1).getString("value"),
                        options.getJSONObject(2).getString("value")));
        for (int i = 0; i < options.size(); i++) {
            JSONObject option = options.getJSONObject(i);
            assertEquals(2, option.size());
            assertTrue(option.containsKey("value"));
            assertTrue(option.containsKey("text"));
        }
        assertEquals("数据仓库", options.getJSONObject(0).getString("text"));
        assertEquals("集成", options.getJSONObject(1).getString("text"));
        assertEquals(before, params);
        assertEquals(0, datawarehouseHandler.calls);
        assertEquals(0, extensionHandler.calls);
        assertEquals(0, integration.calls);
    }

    /** 没有注册类型时保留空数组契约，不补造类型或分页属性。 */
    @Test
    public void shouldReturnEmptyTypeListWhenNoHandlerRegistered() {
        handlers.clear();
        JSONObject result = new ListDashboardDatasourceTypeApi().myDoService(new JSONObject());
        assertEquals(Collections.singleton("tbodyList"), result.keySet());
        assertTrue(result.get("tbodyList") instanceof List);
        assertTrue(result.getJSONArray("tbodyList").isEmpty());
        assertEquals("{\"tbodyList\":[]}", result.toJSONString());
    }

    /** 新接口与既有入口保持 NoAuth，元数据描述对象数组中的 value/text 并归属 tenant。 */
    @Test
    public void shouldDeclareTypeListMetadataAndNoAuth() throws Exception {
        ListDashboardDatasourceTypeApi api = new ListDashboardDatasourceTypeApi();
        assertEquals("dashboard/datasource/type/list", api.getToken());
        assertEquals("nmtadd.dashboarddatasource.typelist", api.getName());
        assertEquals("neatlogic.module.tenant.api.dashboard.datasource", api.getClass().getPackage().getName());
        assertNull(api.getConfig());
        assertFalse(AuthActionChecker.check(DATA_WAREHOUSE_BASE.class));
        validateAuth(api);
        assertEquals(NoAuth.class, api.getClass().getAnnotation(AuthAction.class).action());
        assertEquals(OperationTypeEnum.SEARCH, api.getClass().getAnnotation(OperationType.class).type());
        Method method = api.getClass().getMethod("myDoService", JSONObject.class);
        assertEquals(JSONObject.class, method.getReturnType());
        assertEquals(0, method.getAnnotation(Input.class).value().length);
        assertEquals(api.getName(), method.getAnnotation(Description.class).desc());
        Param[] output = method.getAnnotation(Output.class).value();
        assertEquals(1, output.length);
        assertEquals("tbodyList", output[0].name());
        assertEquals(ApiParamType.JSONARRAY, output[0].type());
        assertEquals(ValueTextVo[].class, output[0].explode());
        assertEquals("nmtadd.dashboarddatasource.typeoptions", output[0].desc());
        assertEquals("nmtadd.dashboarddatasource.typeoptionshelp", output[0].help());
        assertEquals(2, ValueTextVo.class.getDeclaredFields().length);
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

    /** 调用真实 API 方法，并解包反射异常以校验业务异常类型。 */
    private Object invokeApi(PrivateApiComponentBase api, JSONObject params) throws Exception {
        try {
            return api.getClass().getMethod("myDoService", JSONObject.class).invoke(api, params);
        } catch (InvocationTargetException exception) {
            if (exception.getCause() instanceof Exception) {
                throw (Exception) exception.getCause();
            }
            throw exception;
        }
    }

    /** 保留筛选、回显、排序、限制和分页等代表性参数，验证委托入口不改写协议。 */
    private JSONObject requestParams() {
        return JSONObject.parseObject("{\"moduleId\":\"autoexec\",\"keyword\":\"demo\",\"defaultValue\":[10],"
                + "\"idList\":[10],\"isActive\":1,\"id\":10,\"dataSourceId\":10,"
                + "\"conditionList\":[{\"fieldId\":1,\"expression\":\"eq\",\"valueList\":[\"demo\"]}],"
                + "\"sortList\":[{\"fieldId\":2,\"sortType\":\"desc\"}],"
                + "\"limit\":0,\"isExpired\":0,\"currentPage\":2,\"pageSize\":20}");
    }

    /** 仅替代模块 Bean 扫描，注册流程执行父类对真实 ContextRefreshedEvent 的处理。 */
    private void registerHandler(String moduleId, IDashboardDatasourceHandler handler) {
        NeatLogicWebApplicationContext context = new NeatLogicWebApplicationContext() {
            @Override
            public <T> Map<String, T> getBeansOfType(Class<T> type) {
                Map<String, T> beans = new LinkedHashMap<>();
                beans.put("dashboardDatasourceHandler", type.cast(handler));
                return beans;
            }
        };
        context.setId(moduleId);
        new DashboardDatasourceHandlerFactory().onApplicationEvent(new ContextRefreshedEvent(context));
    }

    /** 返回本次新增的三个真实接口实例。 */
    private List<PrivateApiComponentBase> apis() {
        return Arrays.asList(new ListDashboardDatasourceApi(), new GetDashboardDatasourceApi(), new SearchDashboardDatasourceDataApi());
    }

    /** 测试替身只记录委托并返回独立对象，扩展类型不会引入真实集成取数实现。 */
    private static class RecordingHandler implements IDashboardDatasourceHandler {
        private final String type;
        private final JSONObject listResponse = new JSONObject();
        private final JSONObject detailResponse = new JSONObject();
        private final JSONObject dataResponse = new JSONObject();
        private int calls;
        private String lastOperation;
        private JSONObject lastParams;

        /** 为两个注册类型分别建立仅含 fieldList 的详情响应，验证原始 JSON 被直接返回。 */
        private RecordingHandler(String type) {
            this.type = type;
            JSONObject field = new JSONObject();
            field.put("id", 101L);
            field.put("label", type);
            field.put("type", "text");
            field.put("isCondition", 1);
            field.put("inputType", "text");
            field.put("config", JSONObject.parseObject("{\"placeholder\":\"请输入\"}"));
            JSONArray fieldList = new JSONArray();
            fieldList.add(field);
            detailResponse.put("fieldList", fieldList);
        }

        /** 声明可供真实工厂选择的测试类型。 */
        @Override
        public String getType() {
            return type;
        }

        /** 类型选项只读取名称，不应执行任何查询操作。 */
        @Override
        public String getTypeName() {
            return "datawarehouse".equals(type) ? "数据仓库" : "integration".equals(type) ? "集成" : type;
        }

        /** 记录列表操作并返回原始响应实例。 */
        @Override
        public JSONObject listDatasource(JSONObject params) {
            record("listDatasource", params);
            return listResponse;
        }

        /** 记录详情操作并保持最小 JSONObject 返回契约。 */
        @Override
        public JSONObject getDatasource(JSONObject params) {
            record("getDatasource", params);
            return detailResponse;
        }

        /** 记录动态取数操作并返回原始响应实例。 */
        @Override
        public JSONObject searchDatasourceData(JSONObject params) {
            record("searchDatasourceData", params);
            return dataResponse;
        }

        /** 累计调用用于确认未知类型拒绝和不同 Handler 之间无串用。 */
        private void record(String operation, JSONObject params) {
            calls++;
            lastOperation = operation;
            lastParams = params;
        }

        /** 按三个 API 的固定顺序获取预期响应实例。 */
        private Object response(int index) {
            if (index == 0) {
                return listResponse;
            }
            return index == 1 ? detailResponse : dataResponse;
        }
    }
}
