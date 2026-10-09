package neatlogic.module.tenant.api.apimanage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.RequestContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthBase;
import neatlogic.framework.auth.core.AuthFactory;
import neatlogic.framework.auth.label.INTERFACE_MODIFY;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.auth.label.USER_MODIFY;
import neatlogic.framework.dto.AuthVo;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.dao.mapper.ApiMapper;
import neatlogic.framework.restful.dto.ApiHandlerVo;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.framework.restful.enums.ApiType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/** 用内存组件注册表与 Mapper 替身验证声明权限展示，不执行业务接口或修改真实配置。 */
public class ApiManageGetApiTest {
    private final ApiManageGetApi api = new ApiManageGetApi();
    private Map<String, Map<String, Object>> registry;
    private Map<String, Map<String, Object>> originalRegistry;
    private Map<String, ApiVo> originalApis;
    private Map<String, ApiHandlerVo> originalHandlers;
    private Map<String, AuthBase> authMap;
    private Map<String, AuthBase> originalAuths;
    private ApiVo databaseApi;
    private RequestContext originalRequest;

    /** 安装内存查询替身，保留全局注册表及请求上下文供测试结束后恢复。 */
    @Before
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        registry = (Map<String, Map<String, Object>>) field(PrivateApiComponentFactory.class, "componentRegistryMap").get(null);
        originalRegistry = new HashMap<>();
        registry.forEach((type, map) -> originalRegistry.put(type, new HashMap<>(map)));
        originalApis = new HashMap<>(PrivateApiComponentFactory.getApiMap());
        originalHandlers = new HashMap<>(PrivateApiComponentFactory.getApiHandlerMap());
        authMap = (Map<String, AuthBase>) field(AuthFactory.class, "authMap").get(null);
        originalAuths = new HashMap<>(authMap);
        authMap.put("INTERFACE_MODIFY", new INTERFACE_MODIFY());
        authMap.put("USER_MODIFY", new USER_MODIFY());
        ApiMapper mapper = (ApiMapper) Proxy.newProxyInstance(ApiMapper.class.getClassLoader(), new Class<?>[]{ApiMapper.class},
                (proxy, method, args) -> {
                    if ("getApiByTokenWithoutPsw".equals(method.getName())) {
                        return databaseApi;
                    }
                    throw new AssertionError("不应访问其他持久化方法：" + method.getName());
                });
        field(ApiManageGetApi.class, "ApiMapper").set(api, mapper);
        originalRequest = RequestContext.get();
        RequestContext.init((RequestContext) null).setLocale(Locale.CHINESE);
    }

    /** 恢复共享状态，避免污染其他权限及接口回归测试。 */
    @After
    public void teardown() {
        registry.forEach((type, map) -> {
            map.clear();
            map.putAll(originalRegistry.get(type));
        });
        PrivateApiComponentFactory.getApiMap().clear();
        PrivateApiComponentFactory.getApiMap().putAll(originalApis);
        PrivateApiComponentFactory.getApiHandlerMap().clear();
        PrivateApiComponentFactory.getApiHandlerMap().putAll(originalHandlers);
        authMap.clear();
        authMap.putAll(originalAuths);
        RequestContext.get().release();
        if (originalRequest != null) {
            RequestContext.init(originalRequest);
        }
    }

    /** 单项权限复用 AuthVo 的请求语言翻译，并可直接序列化为详情响应。 */
    @Test
    public void translatesSinglePermissionInBothLanguages() throws Exception {
        ApiVo definition = register(new SinglePermission(), ApiType.OBJECT);
        AuthVo auth = get(definition.getToken()).getRequiredAuthList().get(0);
        assertEquals("INTERFACE_MODIFY", auth.getName());
        assertEquals("接口管理权限", auth.getDisplayName());
        RequestContext.get().setLocale(Locale.ENGLISH);
        String english = JSON.parseObject(JSON.toJSONString(get(definition.getToken())))
                .getJSONArray("requiredAuthList").getJSONObject(0).getString("displayName");
        assertEquals("API Management Permission", english);
        assertNull("不得修改工厂中共享的定义", definition.getRequiredAuthList());
    }

    /** 重复注解去重，继承注解及代理组件均从真实目标类读取。 */
    @Test
    public void readsRepeatedInheritedAndProxiedPermissions() throws Exception {
        ProxyFactory proxy = new ProxyFactory(new InheritedPermissions());
        proxy.setProxyTargetClass(true);
        ApiVo definition = register(proxy.getProxy(), ApiType.OBJECT);
        assertEquals(List.of("INTERFACE_MODIFY", "USER_MODIFY"), names(get(definition.getToken())));
    }

    /** 多权限中包含 NoAuth 时直接显示无需接口级功能权限；无注解具有相同语义。 */
    @Test
    public void handlesNoAuthAndNoAnnotations() throws Exception {
        for (Object component : List.of(new Unrestricted(), new Object())) {
            ApiVo definition = register(component, ApiType.OBJECT);
            assertTrue(get(definition.getToken()).getRequiredAuthList().isEmpty());
        }
    }

    /** 名称未加载时保留权限标识，不能把缺失名称误解释为无需权限。 */
    @Test
    public void retainsIdentifierWhenPermissionIsNotRegistered() throws Exception {
        authMap.remove("INTERFACE_MODIFY");
        ApiVo definition = register(new SinglePermission(), ApiType.OBJECT);
        JSONObject response = JSON.parseObject(JSON.toJSONString(get(definition.getToken())));
        assertEquals("INTERFACE_MODIFY", response.getJSONArray("requiredAuthList").getJSONObject(0).getString("name"));
        assertNull(response.getJSONArray("requiredAuthList").getJSONObject(0).getString("displayName"));
    }

    /** 数据库自定义配置必须使用处理器元数据中的类型，覆盖旧配置中的错误类型。 */
    @Test
    public void resolvesCustomHandlerAcrossAllApiTypes() throws Exception {
        for (ApiType type : ApiType.values()) {
            ApiVo definition = register(new SinglePermission(), type);
            PrivateApiComponentFactory.getApiMap().remove(definition.getToken());
            databaseApi = definition;
            databaseApi.setType("old-type");
            ApiHandlerVo handler = new ApiHandlerVo();
            handler.setHandler(definition.getHandler());
            handler.setType(type.getValue());
            PrivateApiComponentFactory.getApiHandlerMap().put(handler.getHandler(), handler);
            assertEquals(List.of("INTERFACE_MODIFY"), names(get(definition.getToken())));
        }
    }

    /** 系统接口的代码处理器优先于数据库旧处理器，读取合并后的实际权限。 */
    @Test
    public void usesCodeHandlerAfterMergingDatabaseConfiguration() throws Exception {
        ApiVo definition = register(new SinglePermission(), ApiType.OBJECT);
        databaseApi = new ApiVo();
        databaseApi.setHandler("stale.handler");
        databaseApi.setNeedAudit(0);
        ApiVo result = get("/" + definition.getToken() + "/");
        assertEquals(definition.getHandler(), result.getHandler());
        assertEquals(Integer.valueOf(0), result.getNeedAudit());
        assertEquals(List.of("INTERFACE_MODIFY"), names(result));
    }

    /** 处理器缺失或类型不可识别时返回未知状态，不使用空列表冒充无权限要求。 */
    @Test
    public void distinguishesUnresolvedHandlerFromUnrestrictedApi() throws Exception {
        databaseApi = new ApiVo();
        databaseApi.setHandler("missing.handler");
        databaseApi.setType(ApiType.OBJECT.getValue());
        assertNull(get("missing/handler").getRequiredAuthList());
        databaseApi.setType("unknown");
        assertNull(get("missing/handler").getRequiredAuthList());
    }

    /** 注册最小接口定义及组件，仅用于读取声明，不调用组件业务方法。 */
    private ApiVo register(Object component, ApiType type) {
        ApiVo definition = new ApiVo();
        definition.setToken("test/required-auth/" + type.getValue());
        definition.setName("nmta.apimanagegetapi.getname");
        definition.setHandlerName("nmta.apimanagegetapi.getname");
        definition.setHandler(component.getClass().getName());
        definition.setType(type.getValue());
        registry.get(type.getValue()).put(definition.getHandler(), component);
        PrivateApiComponentFactory.getApiMap().put(definition.getToken(), definition);
        return definition;
    }

    /** 使用真实详情组装路径，模拟查询参数。 */
    private ApiVo get(String token) throws Exception {
        JSONObject params = new JSONObject();
        params.put("token", token);
        return (ApiVo) api.myDoService(params);
    }

    /** 提取声明权限标识，避免断言依赖无关元数据。 */
    private List<String> names(ApiVo definition) {
        return definition.getRequiredAuthList().stream().map(AuthVo::getName).collect(Collectors.toList());
    }

    /** 访问既有注册表及注入点，测试后恢复原值。 */
    private static Field field(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    /** 单权限组件替身。 */
    @AuthAction(action = INTERFACE_MODIFY.class)
    public static class SinglePermission { }

    /** 重复及多权限组件替身。 */
    @AuthAction(action = INTERFACE_MODIFY.class)
    @AuthAction(action = USER_MODIFY.class)
    @AuthAction(action = INTERFACE_MODIFY.class)
    public static class MultiplePermissions { }

    /** 继承声明的组件替身。 */
    public static class InheritedPermissions extends MultiplePermissions { }

    /** 任一权限可通过时 NoAuth 优先的组件替身。 */
    @AuthAction(action = INTERFACE_MODIFY.class)
    @AuthAction(action = NoAuth.class)
    public static class Unrestricted { }
}
