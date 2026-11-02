package neatlogic.module.tenant.api.datawarehouse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.auth.label.DATA_WAREHOUSE_BASE;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.datawarehouse.dto.DataSourceDataVo;
import neatlogic.framework.datawarehouse.dto.DataSourceFieldVo;
import neatlogic.framework.datawarehouse.dto.DataSourceVo;
import neatlogic.framework.datawarehouse.service.DataSourceService;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.dto.UserAuthVo;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.ApiValidateAndHelpBase;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.$;
import org.junit.Before;
import org.junit.Test;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/** 验证数据仓库 API 仅负责参数转换和服务委托，保留原权限与元数据，不访问数据库或 HTTP。 */
public class DataSourceQueryApiTest {
    private final GetDataSourceApi detailApi = new GetDataSourceApi();
    private final SearchDataSourceApi listApi = new SearchDataSourceApi();
    private final SearchDataSourceDataApi dataApi = new SearchDataSourceDataApi();
    private DataSourceService service;
    private DataSourceVo detailResponse;
    private JSONObject listResponse;
    private JSONObject dataResponse;
    private String lastOperation;
    private Object lastArgument;
    private int serviceCalls;
    private List<UserAuthVo> grants = Collections.emptyList();

    /** 三个真实 API 注入同一个服务替身，未知服务方法立即失败，避免隐含数据库调用。 */
    @Before
    public void initialize() throws Exception {
        detailResponse = new DataSourceVo(101L);
        detailResponse.setName("job_duration");
        detailResponse.setLabel("作业耗时");
        detailResponse.setModuleId("autoexec");
        detailResponse.setXml("<datasource>完整配置</datasource>");
        DataSourceFieldVo field = new DataSourceFieldVo();
        field.setId(7L);
        field.setName("duration");
        field.setLabel("耗时");
        field.setType("number");
        field.setConfig(JSONObject.parseObject("{\"isMultiple\":false,\"nested\":{\"kept\":true}}"));
        detailResponse.setFieldList(Collections.singletonList(field));
        listResponse = JSONObject.parseObject("{\"tbodyList\":[],\"currentPage\":4,\"pageSize\":7,\"rowNum\":19,\"pageCount\":3}");
        dataResponse = JSONObject.parseObject("{\"theadList\":[{\"key\":\"field_7\",\"name\":\"duration\"}],\"tbodyList\":[{\"field_7\":null}],\"currentPage\":2,\"pageSize\":6,\"rowNum\":7,\"pageCount\":2}");
        service = (DataSourceService) Proxy.newProxyInstance(DataSourceService.class.getClassLoader(),
                new Class<?>[]{DataSourceService.class}, (proxy, method, args) -> {
                    serviceCalls++;
                    lastOperation = method.getName();
                    lastArgument = args[0];
                    switch (method.getName()) {
                        case "getDataSourceById": return detailResponse;
                        case "searchDataSource": return listResponse;
                        case "searchDataSourceData": return dataResponse;
                        default: throw new AssertionError("非预期服务调用：" + method.getName());
                    }
                });
        for (PrivateApiComponentBase api : apis()) {
            Field fieldDependency = api.getClass().getDeclaredField("dataSourceService");
            fieldDependency.setAccessible(true);
            fieldDependency.set(api, service);
            assertSame(service, fieldDependency.get(api));
        }
    }

    /** 详情仅按原规则提取 Long ID，完整 VO 及不存在时的 null 都直接返回，不改为字段投影。 */
    @Test
    public void delegatesDetailAndPreservesFullVoAndNull() throws Exception {
        JSONObject params = JSONObject.parseObject("{\"id\":\"101\",\"extra\":null}");
        String before = snapshot(params);
        assertSame(detailResponse, detailApi.myDoService(params));
        assertEquals("getDataSourceById", lastOperation);
        assertEquals(101L, lastArgument);
        assertEquals("job_duration", detailResponse.getName());
        assertEquals("<datasource>完整配置</datasource>", detailResponse.getXml());
        assertTrue(detailResponse.getFieldList().get(0).getConfig().getJSONObject("nested").getBooleanValue("kept"));
        assertEquals(before, snapshot(params));
        detailResponse = null;
        assertNull(detailApi.myDoService(new JSONObject().fluentPut("id", 404L)));
        assertEquals(404L, lastArgument);
        assertEquals(2, serviceCalls);
    }

    /** 列表 DTO 保留筛选、分页、idList 与 defaultValue，回显覆盖规则留给服务，响应对象不重复包装。 */
    @Test
    public void delegatesListWithOriginalFiltersAndEchoParameters() throws Exception {
        JSONObject params = JSONObject.parseObject("{\"keyword\":\"作业\",\"moduleId\":\"autoexec\",\"idList\":[9,8],"
                + "\"defaultValue\":[\"101\",102],\"currentPage\":4,\"pageSize\":7}");
        String before = snapshot(params);
        assertSame(listResponse, listApi.myDoService(params));
        assertEquals("searchDataSource", lastOperation);
        assertTrue(lastArgument instanceof DataSourceVo);
        DataSourceVo query = (DataSourceVo) lastArgument;
        assertEquals("作业", query.getKeyword());
        assertEquals("autoexec", query.getModuleId());
        assertEquals(Arrays.asList(9L, 8L), query.getIdList());
        assertEquals(params.getJSONArray("defaultValue"), query.getDefaultValue());
        assertEquals(Integer.valueOf(4), query.getCurrentPage());
        assertEquals(Integer.valueOf(7), query.getPageSize());
        assertEquals(before, snapshot(params));
        assertEquals(1, serviceCalls);
    }

    /** 取数 DTO 保留空条件值、多字段排序、过期开关与分页参数，API 不预先过滤或组装结果。 */
    @Test
    public void delegatesDataWithConditionsSortExpirationAndPagination() throws Exception {
        JSONObject params = JSONObject.parseObject("{\"dataSourceId\":101,\"conditionList\":[{\"id\":7,\"inputType\":\"text\",\"value\":null},"
                + "{\"id\":8,\"inputType\":\"text\",\"value\":\"\"},{\"id\":9,\"inputType\":\"select\",\"value\":[\"ready\",\"done\"]}],"
                + "\"sortList\":[{\"fieldId\":7,\"type\":\"DESC\"},{\"fieldId\":8,\"type\":\"ASC\"}],"
                + "\"limit\":6,\"isExpired\":1,\"currentPage\":2,\"pageSize\":6,\"defaultValue\":[101]}");
        String before = snapshot(params);
        assertSame(dataResponse, dataApi.myDoService(params));
        assertEquals("searchDataSourceData", lastOperation);
        assertTrue(lastArgument instanceof DataSourceDataVo);
        DataSourceDataVo query = (DataSourceDataVo) lastArgument;
        assertEquals(Long.valueOf(101L), query.getDataSourceId());
        assertEquals(3, query.getConditionList().size());
        assertEquals(Long.valueOf(7L), query.getConditionList().get(0).getId());
        assertNull(query.getConditionList().get(0).getValue());
        assertEquals("", query.getConditionList().get(1).getValue());
        assertEquals(Arrays.asList("ready", "done"), query.getConditionList().get(2).getValue());
        assertEquals("select", query.getConditionList().get(2).getInputType());
        assertEquals(2, query.getSortList().size());
        assertEquals(Long.valueOf(7L), query.getSortList().get(0).getFieldId());
        assertEquals("DESC", query.getSortList().get(0).getType());
        assertEquals(Long.valueOf(8L), query.getSortList().get(1).getFieldId());
        assertEquals("ASC", query.getSortList().get(1).getType());
        assertEquals(Integer.valueOf(6), query.getLimit());
        assertEquals(Integer.valueOf(1), query.getIsExpired());
        assertEquals(Integer.valueOf(2), query.getCurrentPage());
        assertEquals(Integer.valueOf(6), query.getPageSize());
        assertEquals(params.getJSONArray("defaultValue"), query.getDefaultValue());
        assertNull(query.getFieldList());
        assertEquals(before, snapshot(params));
        assertEquals(1, serviceCalls);
    }

    /** limit 缺省、显式 null、0 和正数不被 API 统一转换；缺省分页及过期规则仍由原 DTO 提供。 */
    @Test
    public void preservesMissingNullZeroAndPositiveLimits() throws Exception {
        for (int index = 0; index < 4; index++) {
            JSONObject params = new JSONObject().fluentPut("dataSourceId", 101L);
            Integer expected = index < 2 ? null : index == 2 ? 0 : 5;
            if (index > 0) params.put("limit", expected);
            String before = snapshot(params);
            assertSame(dataResponse, dataApi.myDoService(params));
            DataSourceDataVo query = (DataSourceDataVo) lastArgument;
            assertEquals(expected, query.getLimit());
            assertEquals(Integer.valueOf(0), query.getIsExpired());
            assertEquals(Integer.valueOf(1), query.getCurrentPage());
            assertEquals(Integer.valueOf(20), query.getPageSize());
            assertEquals(index > 0, params.containsKey("limit"));
            assertEquals(before, snapshot(params));
        }
        assertEquals(4, serviceCalls);
    }

    /** 固定原 API 地址、名称、查询操作和参数定义，服务抽取不能改变数据仓库接口契约。 */
    @Test
    public void preservesWarehouseTokensNamesPermissionsAndMetadata() throws Exception {
        String[] tokens = {"datawarehouse/datasource/get", "datawarehouse/datasource/search", "datawarehouse/datasource/data/search"};
        String[] names = {"获取数据仓库数据源", "nmtad.searchdatasourceapi.getname", $.t("nmtad.searchdatasourcedataapi.getname")};
        String[] descriptions = {"获取数据仓库数据源接口", "nmtad.searchdatasourceapi.getname", "nmtad.searchdatasourcedataapi.getname"};
        for (int index = 0; index < apis().size(); index++) {
            PrivateApiComponentBase api = apis().get(index);
            assertEquals(tokens[index], api.getToken());
            assertEquals(names[index], api.getName());
            assertNull(api.getConfig());
            assertNotNull(api.getClass().getAnnotation(Service.class));
            assertEquals(OperationTypeEnum.SEARCH, api.getClass().getAnnotation(OperationType.class).type());
            AuthAction[] actions = api.getClass().getAnnotationsByType(AuthAction.class);
            assertEquals(1, actions.length);
            assertEquals(DATA_WAREHOUSE_BASE.class, actions[0].action());
            Method method = api.getClass().getMethod("myDoService", JSONObject.class);
            assertEquals(Object.class, method.getReturnType());
            assertEquals(descriptions[index], method.getAnnotation(Description.class).desc());
        }
        Param[] detailInput = inputs(detailApi);
        assertEquals(1, detailInput.length);
        assertParam(detailInput[0], "id", ApiParamType.LONG, "id", true, false);
        Param[] listInput = inputs(listApi);
        assertEquals(2, listInput.length);
        assertParam(listInput[0], "moduleId", ApiParamType.STRING, "nfdd.datasourcevo.entityfield.name.moduleid", false, false);
        assertParam(listInput[1], "keyword", ApiParamType.STRING, "common.keyword", false, true);
        Param[] dataInput = inputs(dataApi);
        assertEquals(5, dataInput.length);
        String[] dataNames = {"dataSourceId", "conditionList", "sortList", "limit", "isExpired"};
        ApiParamType[] dataTypes = {ApiParamType.LONG, ApiParamType.JSONARRAY, ApiParamType.JSONARRAY, ApiParamType.INTEGER, ApiParamType.INTEGER};
        String[] suffixes = {"datasourceid", "conditionlist", "sortlist", "limit", "isexpired"};
        for (int index = 0; index < dataInput.length; index++) {
            assertParam(dataInput[index], dataNames[index], dataTypes[index], "nmtad.searchdatasourcedataapi.input.param.desc." + suffixes[index], index == 0, false);
        }
        assertEquals(1, outputs(detailApi).length);
        assertEquals(DataSourceVo.class, outputs(detailApi)[0].explode());
        assertEquals(2, outputs(listApi).length);
        assertEquals(BasePageVo.class, outputs(listApi)[0].explode());
        assertEquals("tbodyList", outputs(listApi)[1].name());
        assertEquals(DataSourceVo[].class, outputs(listApi)[1].explode());
        assertEquals(1, outputs(dataApi).length);
        assertEquals(BasePageVo.class, outputs(dataApi)[0].explode());
        assertEquals(0, serviceCalls);
    }

    /** 调用真实框架鉴权，普通用户缺少数据仓库权限时拒绝，明确授权后通过，静态及线程状态精确恢复。 */
    @Test
    @SuppressWarnings("unchecked")
    public void stillRequiresDataWarehouseBasePermission() throws Exception {
        Field mapperField = accessibleField(AuthActionChecker.class, "userMapper");
        Object originalMapper = mapperField.get(null);
        ThreadLocal<UserContext> users = (ThreadLocal<UserContext>) accessibleField(UserContext.class, "instance").get(null);
        ThreadLocal<TenantContext> tenants = (ThreadLocal<TenantContext>) accessibleField(TenantContext.class, "instance").get(null);
        UserContext originalUser = users.get();
        TenantContext originalTenant = tenants.get();
        try {
            String uuid = "datasource-query-test-user";
            UserContext user = emptyContext(UserContext.class);
            user.setUserUuid(uuid);
            user.setUserId(uuid);
            user.setIsSuperAdmin(false);
            user.setAuthenticationInfoVo(new AuthenticationInfoVo(uuid));
            users.set(user);
            TenantContext tenant = emptyContext(TenantContext.class);
            accessibleField(TenantContext.class, "tenantUuid").set(tenant, "datasource-query-test-tenant");
            tenants.set(tenant);
            UserMapper mapper = (UserMapper) Proxy.newProxyInstance(UserMapper.class.getClassLoader(), new Class<?>[]{UserMapper.class},
                    (proxy, method, args) -> {
                        if ("searchUserAllAuthByUserAuth".equals(method.getName())) return grants;
                        throw new AssertionError("禁止真实数据库授权查询：" + method.getName());
                    });
            new AuthActionChecker().setUserMapper(mapper);
            for (PrivateApiComponentBase api : apis()) {
                assertThrows(PermissionDeniedException.class, () -> validateAuth(api));
            }
            grants = Collections.singletonList(new UserAuthVo(uuid, DATA_WAREHOUSE_BASE.class.getSimpleName()));
            for (PrivateApiComponentBase api : apis()) validateAuth(api);
            assertEquals(0, serviceCalls);
        } finally {
            mapperField.set(null, originalMapper);
            if (originalUser == null) users.remove(); else users.set(originalUser);
            if (originalTenant == null) tenants.remove(); else tenants.set(originalTenant);
        }
    }

    /** 返回同一组已注入服务替身的真实 API。 */
    private List<PrivateApiComponentBase> apis() { return Arrays.asList(detailApi, listApi, dataApi); }

    /** 快照显式保留 null 并比较 JSON 内容，避免解析时 Long/Integer 装箱差异造成误判。 */
    private String snapshot(JSONObject params) { return JSON.toJSONString(params, SerializerFeature.WriteMapNullValue); }

    /** 读取真实接口输入元数据，断言顺序和类型保持不变。 */
    private Param[] inputs(PrivateApiComponentBase api) throws Exception {
        return api.getClass().getMethod("myDoService", JSONObject.class).getAnnotation(Input.class).value();
    }

    /** 读取真实接口输出元数据，保留完整数据仓库 DTO 声明。 */
    private Param[] outputs(PrivateApiComponentBase api) throws Exception {
        return api.getClass().getMethod("myDoService", JSONObject.class).getAnnotation(Output.class).value();
    }

    /** 比较本次需保留的输入参数属性，不依赖 HTTP 文档生成过程。 */
    private void assertParam(Param param, String name, ApiParamType type, String desc, boolean required, boolean xss) {
        assertEquals(name, param.name());
        assertEquals(type, param.type());
        assertEquals(desc, param.desc());
        assertEquals(required, param.isRequired());
        assertEquals(xss, param.xss());
    }

    /** 读取已有依赖和线程容器以便恢复，不修改生产可见性。 */
    private Field accessibleField(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    /** 构造隔离上下文，不经过全局初始化避免修改日志 MDC 或访问租户配置。 */
    private <T> T emptyContext(Class<T> type) throws Exception {
        Constructor<T> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    /** 使用框架原权限方法并解包反射异常，保证断言的是实际拒绝类型。 */
    private void validateAuth(PrivateApiComponentBase api) throws Exception {
        Method method = ApiValidateAndHelpBase.class.getDeclaredMethod("validAuth", Class.class);
        method.setAccessible(true);
        try {
            method.invoke(api, api.getClass());
        } catch (InvocationTargetException exception) {
            if (exception.getCause() instanceof Exception) throw (Exception) exception.getCause();
            throw exception;
        }
    }
}
