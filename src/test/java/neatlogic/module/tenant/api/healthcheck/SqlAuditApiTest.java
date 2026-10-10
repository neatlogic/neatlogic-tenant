package neatlogic.module.tenant.api.healthcheck;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dao.plugin.SqlCostInterceptor;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.dto.healthcheck.RequestSqlAuditVo;
import neatlogic.framework.dto.healthcheck.SqlAuditVo;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.healthcheck.SqlAuditManager;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.dto.ApiVo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

/** 验证 SQL 审计接口排序、过滤分页、模式切换及真实框架校验与权限拒绝路径。 */
public class SqlAuditApiTest {
    private final SearchSqlAuditApi searchApi = new SearchSqlAuditApi();
    private final UpdateSqlAuditModeApi updateApi = new UpdateSqlAuditModeApi();
    private Field checkerMapperField;
    private Object originalCheckerMapper;

    /** 清理缓存并安装拒绝普通用户授权的 Mapper 替身，不访问数据库。 */
    @Before
    public void setup() throws Exception {
        SqlAuditManager.clearAllSqlAudit();
        SqlAuditManager.updateSaveMode("recent");
        SqlCostInterceptor.SqlIdMap.clear();
        SqlCostInterceptor.UrlMap.clear();
        checkerMapperField = AuthActionChecker.class.getDeclaredField("userMapper");
        checkerMapperField.setAccessible(true);
        originalCheckerMapper = checkerMapperField.get(null);
        UserMapper mapper = (UserMapper) Proxy.newProxyInstance(UserMapper.class.getClassLoader(),
                new Class<?>[]{UserMapper.class}, (proxy, method, args) -> {
                    if ("searchUserAllAuthByUserAuth".equals(method.getName())) {
                        return Collections.emptyList();
                    }
                    throw new AssertionError("不应访问其他用户 Mapper 方法：" + method.getName());
                });
        checkerMapperField.set(null, mapper);
        TenantContext.init("sqlaudit-test");
        initUser(true);
    }

    /** 恢复静态依赖和上下文，避免测试影响其他 API 的授权。 */
    @After
    public void cleanup() throws Exception {
        SqlAuditManager.clearAllSqlAudit();
        SqlAuditManager.updateSaveMode("recent");
        SqlCostInterceptor.SqlIdMap.clear();
        SqlCostInterceptor.UrlMap.clear();
        checkerMapperField.set(null, originalCheckerMapper);
        if (UserContext.get() != null) {
            UserContext.get().release();
        }
        TenantContext.get().release();
    }

    /** 两类记录默认按发生时间倒序，切换慢模式后默认按各自耗时倒序。 */
    @Test
    public void shouldSelectDefaultOrderingFromSaveMode() throws Exception {
        addRecord("old-slow", 1, 1, 100, "tenant-a", "user-a");
        addRecord("new-fast", 2, 2, 10, "tenant-a", "user-a");
        JSONObject recent = query(new JSONObject());
        Assert.assertEquals("recent", recent.getString("saveMode"));
        assertFirst(recent, "new-fast", 2);

        JSONObject changed = (JSONObject) updateApi.myDoService(mode("slowest"));
        Assert.assertEquals("slowest", changed.getString("saveMode"));
        JSONObject slowest = query(new JSONObject());
        Assert.assertEquals("slowest", slowest.getString("saveMode"));
        assertFirst(slowest, "old-slow", 1);
        Assert.assertEquals(2, slowest.getJSONObject("sqlAuditData").getIntValue("rowNum"));
        Assert.assertEquals(2, slowest.getJSONObject("requestSqlAuditData").getIntValue("rowNum"));
    }

    /** 显式排序继续优先于保存模式，耗时相同的记录保留快照中的顺序。 */
    @Test
    public void shouldPreserveExplicitOrderingAndStableTies() throws Exception {
        addRecord("old-slow", 1, 1, 100, "tenant-a", "user-a");
        addRecord("new-fast", 2, 2, 10, "tenant-a", "user-a");
        JSONObject param = new JSONObject();
        param.put("orderBy", "timecost");
        assertFirst(query(param), "old-slow", 1);
        updateApi.myDoService(mode("slowest"));
        param.put("orderBy", "runtime");
        assertFirst(query(param), "new-fast", 2);
        addRecord("equal-slow", 3, 3, 100, "tenant-a", "user-a");
        SqlAuditManager.Snapshot snapshot = SqlAuditManager.getSnapshot();
        JSONObject result = query(new JSONObject());
        Assert.assertEquals(snapshot.getSqlAuditList().get(0).getId(), rows(result, "sqlAuditData").getJSONObject(0).getString("id"));
        Assert.assertEquals(snapshot.getRequestSqlAuditList().get(0).getId(), rows(result, "requestSqlAuditData").getJSONObject(0).getLong("id"));
    }

    /** 两类表头均支持按耗时和发生时间升降序，排序不改变保留模式和最大耗时。 */
    @Test
    public void shouldSupportAscendingAndDescendingForBothTables() throws Exception {
        addRecord("old-slow", 1, 1, 100, "tenant-a", "user-a");
        addRecord("new-fast", 2, 2, 10, "tenant-a", "user-a");
        JSONObject param = new JSONObject();
        param.put("orderBy", "timecost");
        param.put("orderType", "ASC");
        JSONObject result = query(param);
        assertFirst(result, "new-fast", 2);
        Assert.assertEquals("recent", result.getString("saveMode"));
        Assert.assertEquals(100, result.getLongValue("maxTimeCost"));
        Assert.assertEquals(100, result.getLongValue("maxRequestTimeCost"));
        param.put("orderType", "DESC");
        assertFirst(query(param), "old-slow", 1);
        param.put("orderBy", "runtime");
        param.put("orderType", "ASC");
        assertFirst(query(param), "old-slow", 1);
        param.put("orderType", "DESC");
        assertFirst(query(param), "new-fast", 2);
        updateApi.myDoService(mode("slowest"));
        param.remove("orderBy");
        param.put("orderType", "ASC");
        assertFirst(query(param), "new-fast", 2);
    }

    /** 升降序都保留同耗时记录的快照顺序，不采用整张结果列表反转。 */
    @Test
    public void shouldKeepSnapshotOrderForEqualCostsInBothDirections() throws Exception {
        addRecord("first", 1, 1, 100, "tenant-a", "user-a");
        addRecord("second", 2, 2, 100, "tenant-a", "user-a");
        for (String direction : Arrays.asList("ASC", "DESC")) {
            JSONObject param = new JSONObject();
            param.put("orderBy", "timecost");
            param.put("orderType", direction);
            SqlAuditManager.Snapshot snapshot = SqlAuditManager.getSnapshot();
            JSONObject result = query(param);
            assertFirst(result, snapshot.getSqlAuditList().get(0).getId(), snapshot.getRequestSqlAuditList().get(0).getId());
        }
    }

    /** 真实入口拒绝非法方向，兼容省略方向及大小写不同的合法枚举。 */
    @Test
    public void shouldValidateOrderTypeAndKeepLegacyDefaultInFrameworkExecution() throws Exception {
        addRecord("old-slow", 1, 1, 100, "tenant-a", "user-a");
        addRecord("new-fast", 2, 2, 10, "tenant-a", "user-a");
        JSONObject param = new JSONObject();
        param.put("orderBy", "timecost");
        assertFirst((JSONObject) searchApi.doService(apiVo(searchApi), param, null), "old-slow", 1);
        param.put("orderType", "unsupported");
        try {
            searchApi.doService(apiVo(searchApi), param, null);
            Assert.fail("必须拒绝枚举外的排序方向");
        } catch (ParamIrregularException expected) {
            Assert.assertEquals("recent", SqlAuditManager.getSaveMode());
        }
        param.put("orderBy", "TIMECOST");
        param.put("orderType", "asc");
        assertFirst((JSONObject) searchApi.doService(apiVo(searchApi), param, null), "new-fast", 2);
        param.put("orderType", "desc");
        assertFirst((JSONObject) searchApi.doService(apiVo(searchApi), param, null), "old-slow", 1);
        param.remove("orderBy");
        param.remove("orderType");
        assertFirst((JSONObject) searchApi.doService(apiVo(searchApi), param, null), "new-fast", 2);
        updateApi.myDoService(mode("slowest"));
        assertFirst((JSONObject) searchApi.doService(apiVo(searchApi), param, null), "old-slow", 1);
    }

    /** 两类数据共用过滤条件但独立分页，最大耗时按过滤结果计算并限制越界页码。 */
    @Test
    public void shouldKeepFiltersPaginationAndMaximumCosts() throws Exception {
        for (int i = 1; i <= 25; i++) {
            addRecord("mapper.Target" + i, i, i, i, "tenant-a", "user-a");
        }
        addRecord("mapper.TargetOther", 26, 26, 999, "tenant-b", "user-a");
        addRecord("mapper.TargetAnother", 27, 27, 999, "tenant-a", "user-b");
        addRecord("mapper.Unrelated", 28, 28, 999, "tenant-a", "user-a");
        updateApi.myDoService(mode("slowest"));
        JSONObject param = new JSONObject();
        param.put("keyword", "TARGET");
        param.put("tenant", "tenant-a");
        param.put("userId", "user-a");
        param.put("currentPage", 2);
        param.put("requestCurrentPage", 1);
        JSONObject result = query(param);
        JSONObject sqlData = result.getJSONObject("sqlAuditData");
        JSONObject requestData = result.getJSONObject("requestSqlAuditData");
        Assert.assertEquals(25, sqlData.getIntValue("rowNum"));
        Assert.assertEquals(25, requestData.getIntValue("rowNum"));
        Assert.assertEquals(2, sqlData.getIntValue("currentPage"));
        Assert.assertEquals(1, requestData.getIntValue("currentPage"));
        Assert.assertEquals(5, rows(result, "sqlAuditData").size());
        Assert.assertEquals(20, rows(result, "requestSqlAuditData").size());
        Assert.assertEquals(25, result.getLongValue("maxTimeCost"));
        Assert.assertEquals(25, result.getLongValue("maxRequestTimeCost"));
        param.put("currentPage", 999);
        param.put("requestCurrentPage", -1);
        result = query(param);
        Assert.assertEquals(2, result.getJSONObject("sqlAuditData").getIntValue("currentPage"));
        Assert.assertEquals(1, result.getJSONObject("requestSqlAuditData").getIntValue("currentPage"));
        param.put("keyword", "missing");
        result = query(param);
        Assert.assertEquals(0, result.getJSONObject("sqlAuditData").getIntValue("rowNum"));
        Assert.assertEquals(1, result.getJSONObject("sqlAuditData").getIntValue("currentPage"));
    }

    /** 模式往返保留记录，请求聚合详情不丢失；清空仍清除监控目标但不重置模式。 */
    @Test
    public void shouldRetainDataAcrossSwitchesAndKeepModeWhenClearing() throws Exception {
        addRecord("mapper.sql", 1, 1, 100, "tenant-a", "user-a");
        RequestSqlAuditVo request = new RequestSqlAuditVo(2L, "/target/multi", "thread");
        SqlAuditVo uncached = sql("mapper.multi", 2, 30, "tenant-a", "user-a");
        SqlAuditVo cached = sql("mapper.multi", 3, 40, "tenant-a", "user-a");
        cached.setUseCacheLevel("SESSION");
        request.addSqlAudit(uncached);
        request.addSqlAudit(cached);
        SqlAuditManager.addRequestSqlAudit(request);
        for (String saveMode : Arrays.asList("slowest", "recent", "slowest")) {
            updateApi.myDoService(mode(saveMode));
            JSONObject result = query(new JSONObject());
            Assert.assertEquals(1, result.getJSONObject("sqlAuditData").getIntValue("rowNum"));
            Assert.assertEquals(2, result.getJSONObject("requestSqlAuditData").getIntValue("rowNum"));
        }
        RequestSqlAuditVo aggregate = SqlAuditManager.getRequestSqlAuditList().stream()
                .filter(row -> row.getId().equals(2L)).findFirst().get();
        Assert.assertEquals(2, aggregate.getSqlCount());
        Assert.assertEquals(70, aggregate.getTotalTimeCost());
        Assert.assertEquals(30, aggregate.getNotUseCacheTotalTimeCost());
        Assert.assertEquals(2, aggregate.getSameIdSqlAuditList().get(0).getSqlAuditList().size());
        SqlCostInterceptor.SqlIdMap.addId("sql");
        SqlCostInterceptor.UrlMap.addUrl("/target/multi");
        JSONObject clear = new JSONObject();
        clear.put("action", "clear");
        new ToggleSqlInterceptorApi().myDoService(clear);
        JSONObject result = query(new JSONObject());
        Assert.assertEquals("slowest", result.getString("saveMode"));
        Assert.assertEquals(0, result.getJSONObject("sqlAuditData").getIntValue("rowNum"));
        Assert.assertEquals(0, result.getJSONObject("requestSqlAuditData").getIntValue("rowNum"));
        Assert.assertTrue(result.getJSONArray("sqlIdList").isEmpty());
        Assert.assertTrue(result.getJSONArray("urlList").isEmpty());
    }

    /** 从真实执行入口检查必填与枚举规则，校验失败必须发生在模式更新之前。 */
    @Test
    public void shouldApplyRequiredAndEnumValidationInFrameworkExecution() throws Exception {
        try {
            updateApi.doService(apiVo(updateApi), new JSONObject(), null);
            Assert.fail("必须拒绝缺失保存模式");
        } catch (ParamNotExistsException expected) {
            Assert.assertEquals("recent", SqlAuditManager.getSaveMode());
        }
        try {
            updateApi.doService(apiVo(updateApi), mode("unsupported"), null);
            Assert.fail("必须拒绝枚举外的保存模式");
        } catch (ParamIrregularException expected) {
            Assert.assertEquals("recent", SqlAuditManager.getSaveMode());
        }
        JSONObject result = (JSONObject) updateApi.doService(apiVo(updateApi), mode("slowest"), null);
        Assert.assertEquals("slowest", result.getString("saveMode"));
        Assert.assertEquals("slowest", ((JSONObject) updateApi.doService(apiVo(updateApi), mode("SLOWEST"), null)).getString("saveMode"));
    }

    /** 未获得管理员权限的普通用户在真实入口拒绝查询与更新，不能修改保存策略。 */
    @Test
    public void shouldDenyQueriesAndUpdatesWithoutAdminPermission() throws Exception {
        initUser(false);
        for (PrivateApiComponentBase api : Arrays.asList(searchApi, updateApi)) {
            try {
                api.doService(apiVo(api), mode("slowest"), null);
                Assert.fail("没有管理员权限时必须拒绝 SQL 监控接口");
            } catch (PermissionDeniedException expected) {
                Assert.assertEquals("recent", SqlAuditManager.getSaveMode());
            }
        }
    }

    /** 建立普通测试身份，超级管理员用于隔离合法参数的授权分支。 */
    private void initUser(boolean superAdmin) {
        UserVo user = new UserVo();
        user.setUuid("sqlaudit-test-user");
        user.setUserId("sqlaudit-test-user");
        user.setIsSuperAdmin(superAdmin);
        user.setAuthorization("test-authorization");
        UserContext.init(user, new AuthenticationInfoVo(user.getUuid()));
    }

    /** 创建无审计副作用的真实执行入口描述。 */
    private ApiVo apiVo(PrivateApiComponentBase api) {
        ApiVo result = new ApiVo();
        result.setToken(api.getToken());
        result.setModuleId("tenant");
        result.setNeedAudit(0);
        result.setIsActive(1);
        return result;
    }

    /** 构造模式更新参数。 */
    private JSONObject mode(String value) {
        JSONObject result = new JSONObject();
        result.put("saveMode", value);
        return result;
    }

    /** 同时写入单条 SQL 与对应请求，便于比较两种排序。 */
    private void addRecord(String id, long requestId, long runTime, long cost, String tenant, String user) {
        SqlAuditVo record = sql(id, runTime, cost, tenant, user);
        SqlAuditManager.addSqlAudit(record);
        RequestSqlAuditVo request = new RequestSqlAuditVo(requestId, "/audit/" + id, "thread");
        request.addSqlAudit(record);
        SqlAuditManager.addRequestSqlAudit(request);
    }

    /** 创建携带完整过滤字段的 SQL 明细。 */
    private SqlAuditVo sql(String id, long runTime, long cost, String tenant, String user) {
        SqlAuditVo result = new SqlAuditVo();
        result.setId(id);
        result.setRunTime(new Date(runTime));
        result.setTimeCost(cost);
        result.setTenant(tenant);
        result.setUserId(user);
        result.setSql("select 1");
        return result;
    }

    /** 调用查询业务并保持 JSON 表格响应契约。 */
    private JSONObject query(JSONObject param) throws Exception {
        return (JSONObject) searchApi.myDoService(param);
    }

    /** 读取独立表格的 JSON 行列表。 */
    private JSONArray rows(JSONObject result, String table) {
        return result.getJSONObject(table).getJSONArray("tbodyList");
    }

    /** 断言两类记录的第一行均遵循预期排序。 */
    private void assertFirst(JSONObject result, String sqlId, long requestId) {
        Assert.assertEquals(sqlId, rows(result, "sqlAuditData").getJSONObject(0).getString("id"));
        Assert.assertEquals(requestId, rows(result, "requestSqlAuditData").getJSONObject(0).getLongValue("id"));
    }
}
