package neatlogic.module.tenant.api.healthcheck;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dao.mapper.healthcheck.SqlStatusMapper;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.framework.store.mysql.DatasourceManager;
import neatlogic.module.tenant.service.healthcheck.SqlExplainServiceImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;

/** 验证结构化执行计划的可选查询、回退、真实参数校验和管理员权限。 */
public class GetSqlExplainApiTest {
    private final GetSqlExplainApi api = new GetSqlExplainApi();
    private final List<String> queries = new ArrayList<>();
    private final List<Map<String, Object>> traditionalRows = new ArrayList<>();
    private Field databaseField;
    private Object originalDatabase;
    private Field checkerField;
    private Object originalChecker;
    private Object jsonValue = "{\"query_block\":{\"select_id\":1,\"table\":{\"table_name\":\"a\"}}}";
    private String jsonColumn = "EXPLAIN";
    private boolean jsonQueryFails;
    private boolean traditionalQueryFails;
    private boolean emptyJsonRows;

    /** 注入 Mapper 替身并隔离静态数据库类型和认证上下文。 */
    @Before
    public void setup() throws Exception {
        databaseField = DatasourceManager.class.getDeclaredField("databaseId");
        databaseField.setAccessible(true);
        originalDatabase = databaseField.get(null);
        databaseField.set(null, "mysql");
        checkerField = AuthActionChecker.class.getDeclaredField("userMapper");
        checkerField.setAccessible(true);
        originalChecker = checkerField.get(null);
        checkerField.set(null, Proxy.newProxyInstance(UserMapper.class.getClassLoader(),
                new Class<?>[]{UserMapper.class}, (proxy, method, args) -> Collections.emptyList()));
        SqlStatusMapper mapper = (SqlStatusMapper) Proxy.newProxyInstance(SqlStatusMapper.class.getClassLoader(),
                new Class<?>[]{SqlStatusMapper.class}, (proxy, method, args) -> {
                    Assert.assertEquals("selectListBySql", method.getName());
                    String sql = (String) args[0];
                    queries.add(sql);
                    if (sql.startsWith("EXPLAIN FORMAT=JSON ")) {
                        if (jsonQueryFails) {
                            throw new IllegalStateException("JSON 查询暂不可用");
                        }
                        if (emptyJsonRows) {
                            return Collections.emptyList();
                        }
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put(jsonColumn, jsonValue);
                        return Collections.singletonList(row);
                    }
                    if (traditionalQueryFails) {
                        throw new IllegalStateException("传统计划查询失败");
                    }
                    return traditionalRows;
                });
        // 执行计划逻辑下沉到 Service，API 继续验证真实参数及权限契约。
        SqlExplainServiceImpl service = new SqlExplainServiceImpl();
        Field mapperField = SqlExplainServiceImpl.class.getDeclaredField("sqlStatusMapper");
        mapperField.setAccessible(true);
        mapperField.set(service, mapper);
        Field serviceField = GetSqlExplainApi.class.getDeclaredField("sqlExplainService");
        serviceField.setAccessible(true);
        serviceField.set(api, service);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1);
        row.put("select_type", "SIMPLE");
        row.put("table", "a");
        row.put("type", "const");
        traditionalRows.add(row);
        TenantContext.init("sql-explain-test");
        initUser(true);
    }

    /** 恢复测试前的静态依赖，避免影响其他审计接口测试。 */
    @After
    public void cleanup() throws Exception {
        databaseField.set(null, originalDatabase);
        checkerField.set(null, originalChecker);
        if (UserContext.get() != null) {
            UserContext.get().release();
        }
        TenantContext.get().release();
    }

    /** 缺省及显式关闭时保持旧响应和一次查询。 */
    @Test
    public void shouldKeepLegacyQueryWhenJsonIsNotRequested() throws Exception {
        JSONObject params = params(null);
        JSONObject result = execute(params);
        Assert.assertEquals(Integer.valueOf(0), params.getInteger("includePlanJson"));
        assertTraditional(result);
        Assert.assertFalse(result.containsKey("planJson"));
        Assert.assertEquals(Collections.singletonList("EXPLAIN SELECT a.id FROM ai_agent a"), queries);
        queries.clear();
        assertTraditional(execute(params(0)));
        Assert.assertEquals(1, queries.size());
    }

    /** 开启时只追加预估 JSON 查询，并将文本解析为对象。 */
    @Test
    public void shouldReturnParsedJsonAndPreserveTraditionalData() throws Exception {
        JSONObject result = execute(params(1));
        assertTraditional(result);
        Assert.assertEquals("a", result.getJSONObject("planJson").getJSONObject("query_block")
                .getJSONObject("table").getString("table_name"));
        Assert.assertEquals(2, queries.size());
        Assert.assertEquals("EXPLAIN FORMAT=JSON SELECT a.id FROM ai_agent a", queries.get(1));
        Assert.assertTrue(queries.stream().noneMatch(sql -> sql.contains("ANALYZE")));
    }

    /** 数据库驱动返回小写列名时仍能读取 JSON。 */
    @Test
    public void shouldAcceptCaseInsensitiveExplainColumn() throws Exception {
        jsonColumn = "explain";
        Assert.assertNotNull(execute(params(1)).getJSONObject("planJson"));
    }

    /** JSON 查询失败不会吞掉已取得的传统计划。 */
    @Test
    public void shouldFallbackWhenJsonQueryFails() throws Exception {
        jsonQueryFails = true;
        JSONObject result = execute(params(1));
        assertTraditional(result);
        Assert.assertFalse(result.containsKey("planJson"));
        Assert.assertEquals(2, queries.size());
    }

    /** 无效文本、数组、空对象和空值都只回退图形数据。 */
    @Test
    public void shouldFallbackWhenJsonCannotBeParsedAsNonemptyObject() throws Exception {
        for (Object invalid : new Object[]{"invalid json", "[]", "{}", "null", null}) {
            jsonValue = invalid;
            JSONObject result = execute(params(1));
            assertTraditional(result);
            Assert.assertFalse(result.containsKey("planJson"));
        }
    }

    /** JSON 查询未返回计划列或结果行时保留传统明细。 */
    @Test
    public void shouldFallbackWhenJsonResultIsMissing() throws Exception {
        jsonColumn = "other";
        Assert.assertFalse(execute(params(1)).containsKey("planJson"));
        emptyJsonRows = true;
        JSONObject result = execute(params(1));
        assertTraditional(result);
        Assert.assertFalse(result.containsKey("planJson"));
    }

    /** 非 MySQL 沿用数据库自身的表头，不发送 MySQL 专属语法。 */
    @Test
    public void shouldKeepNonMysqlPlanWithoutJsonQuery() throws Exception {
        databaseField.set(null, "oracle");
        JSONObject result = execute(params(1));
        Assert.assertEquals(1, queries.size());
        Assert.assertEquals(4, result.getJSONArray("theadList").size());
        Assert.assertEquals(1, result.getIntValue("rowNum"));
        Assert.assertFalse(result.containsKey("planJson"));
    }

    /** 没有 SQL 时不新增任何数据库调用。 */
    @Test
    public void shouldNotQueryEmptySql() throws Exception {
        JSONObject params = params(1);
        params.put("sql", " ");
        JSONObject result = execute(params);
        Assert.assertTrue(queries.isEmpty());
        Assert.assertEquals(0, result.getIntValue("rowNum"));
        Assert.assertFalse(result.containsKey("planJson"));
    }

    /** 主查询错误仍向上传递，JSON 的回退逻辑只处理附加查询。 */
    @Test
    public void shouldPropagateTraditionalPlanQueryFailure() throws Exception {
        traditionalQueryFails = true;
        try {
            execute(params(1));
            Assert.fail("传统计划失败必须抛出异常");
        } catch (IllegalStateException expected) {
            Assert.assertEquals(1, queries.size());
        }
    }

    /** 框架拒绝非法整数和值域，不允许业务查询在校验前执行。 */
    @Test
    public void shouldValidateFlagInFrameworkExecution() throws Exception {
        for (Object invalid : new Object[]{-1, 2, 0.5, "unsupported"}) {
            try {
                execute(params(invalid));
                Assert.fail("框架必须拒绝非法开关");
            } catch (ParamIrregularException expected) {
                Assert.assertTrue(queries.isEmpty());
            }
        }
        Assert.assertNotNull(execute(params("1")).getJSONObject("planJson"));
    }

    /** 普通用户无法获取传统或 JSON 计划。 */
    @Test
    public void shouldDenyNonAdminBeforeQuerying() throws Exception {
        initUser(false);
        try {
            execute(params(1));
            Assert.fail("必须拒绝普通用户");
        } catch (PermissionDeniedException expected) {
            Assert.assertTrue(queries.isEmpty());
        }
    }

    /** 建立带权限状态的测试身份。 */
    private void initUser(boolean superAdmin) {
        if (UserContext.get() != null) {
            UserContext.get().release();
        }
        UserVo user = new UserVo();
        user.setUuid("sql-explain-test-user");
        user.setUserId("sql-explain-test-user");
        user.setIsSuperAdmin(superAdmin);
        user.setAuthorization("test-authorization");
        UserContext.init(user, new AuthenticationInfoVo(user.getUuid()));
    }

    /** 构造可选开关请求。 */
    private JSONObject params(Object flag) {
        JSONObject result = new JSONObject();
        result.put("sql", "SELECT a.id FROM ai_agent a");
        if (flag != null) {
            result.put("includePlanJson", flag);
        }
        return result;
    }

    /** 经过真实接口执行入口应用框架校验和授权。 */
    private JSONObject execute(JSONObject params) throws Exception {
        ApiVo apiVo = new ApiVo();
        apiVo.setToken(api.getToken());
        apiVo.setModuleId("tenant");
        apiVo.setNeedAudit(0);
        apiVo.setIsActive(1);
        return (JSONObject) api.doService(apiVo, params, null);
    }

    /** 断言传统计划原有字段、分页与空字段省略行完整保留。 */
    private void assertTraditional(JSONObject result) {
        Assert.assertEquals("SELECT a.id FROM ai_agent a", result.getString("sql"));
        Assert.assertEquals(12, result.getJSONArray("theadList").size());
        Assert.assertEquals(1, result.getIntValue("rowNum"));
        Assert.assertEquals(1, result.getIntValue("currentPage"));
        Assert.assertEquals(1, result.getIntValue("pageCount"));
        Assert.assertEquals(20, result.getIntValue("pageSize"));
        Assert.assertSame(traditionalRows, result.get("tbodyList"));
    }
}
