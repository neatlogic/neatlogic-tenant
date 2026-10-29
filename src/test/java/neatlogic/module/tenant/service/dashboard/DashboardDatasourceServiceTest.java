package neatlogic.module.tenant.service.dashboard;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceDataMapper;
import neatlogic.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceMapper;
import neatlogic.framework.datawarehouse.dto.DataSourceDataVo;
import neatlogic.framework.datawarehouse.dto.DataSourceFieldVo;
import neatlogic.framework.datawarehouse.dto.DataSourceVo;
import neatlogic.framework.datawarehouse.exceptions.DataSourceIsNotFoundException;
import neatlogic.module.tenant.exception.dashboard.DashboardDatasourceTypeNotSupportedException;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/** 使用真实 DTO 和服务、代理 Mapper 验证旧数据仓库查询契约，无需业务数据库。 */
public class DashboardDatasourceServiceTest {
    private DashboardDatasourceServiceImpl service;
    private DataSourceVo datasource;
    private List<DataSourceVo> datasourceList;
    private List<HashMap<String, Object>> rows;
    private DataSourceVo listQuery;
    private DataSourceDataVo dataQuery;
    private Long detailId;
    private int queryCount;
    private int listCountQueryCount;

    /** 初始化字段与结果样例，捕获实际传给 Mapper 的查询对象。 */
    @Before
    public void initialize() throws Exception {
        service = new DashboardDatasourceServiceImpl();
        datasource = new DataSourceVo();
        datasource.setId(10L);
        datasource.setName("job_duration");
        datasource.setLabel("子作业耗时");
        DataSourceFieldVo nameField = new DataSourceFieldVo();
        nameField.setId(101L);
        nameField.setName("jobName");
        nameField.setLabel("作业名");
        nameField.setType("text");
        DataSourceFieldVo valueField = new DataSourceFieldVo();
        valueField.setId(102L);
        valueField.setName("duration");
        valueField.setLabel("耗时");
        valueField.setType("number");
        datasource.setFieldList(Arrays.asList(nameField, valueField));
        datasourceList = Collections.singletonList(datasource);
        HashMap<String, Object> row = new HashMap<>();
        row.put("id", 1001L);
        row.put("field_101", "应用一");
        row.put("field_102", 12.5);
        rows = Collections.singletonList(row);
        DataWarehouseDataSourceMapper sourceMapper = (DataWarehouseDataSourceMapper) Proxy.newProxyInstance(
                DataWarehouseDataSourceMapper.class.getClassLoader(), new Class<?>[]{DataWarehouseDataSourceMapper.class},
                (proxy, method, args) -> {
                    queryCount++;
                    switch (method.getName()) {
                        case "searchDataSource":
                            listQuery = (DataSourceVo) args[0];
                            return datasourceList;
                        case "searchDataSourceCount":
                            listCountQueryCount++;
                            return 41;
                        case "getDataSourceById":
                            detailId = (Long) args[0];
                            return datasource;
                        default:
                            throw new AssertionError("非预期 Mapper 调用：" + method.getName());
                    }
                });
        DataWarehouseDataSourceDataMapper dataMapper = (DataWarehouseDataSourceDataMapper) Proxy.newProxyInstance(
                DataWarehouseDataSourceDataMapper.class.getClassLoader(), new Class<?>[]{DataWarehouseDataSourceDataMapper.class},
                (proxy, method, args) -> {
                    queryCount++;
                    dataQuery = (DataSourceDataVo) args[0];
                    if ("searchDataSourceDataCount".equals(method.getName())) {
                        return rows.size();
                    }
                    if ("searchDataSourceData".equals(method.getName())) {
                        return rows;
                    }
                    throw new AssertionError("非预期 Mapper 调用：" + method.getName());
                });
        inject(service, "dataSourceMapper", sourceMapper);
        inject(service, "dataSourceDataMapper", dataMapper);
    }

    /** 列表原有筛选、分页和 defaultValue 回显保持不变，结果不二次包装。 */
    @Test
    public void shouldPreserveListFiltersPaginationAndSelectedValues() {
        JSONObject params = JSON.parseObject("{\"datasourceType\":\"datawarehouse\",\"keyword\":\"job\",\"moduleId\":\"autoexec\",\"isActive\":1,\"currentPage\":2,\"pageSize\":10,\"idList\":[999],\"defaultValue\":[\"10\",20]}");
        JSONObject result = service.listDatasource(params);
        assertEquals("job", listQuery.getKeyword());
        assertEquals("autoexec", listQuery.getModuleId());
        assertEquals(Integer.valueOf(1), listQuery.getIsActive());
        assertEquals(Arrays.asList(10L, 20L), listQuery.getIdList());
        assertEquals(Integer.valueOf(10), listQuery.getStartNum());
        assertEquals(2, result.getIntValue("currentPage"));
        assertEquals(10, result.getIntValue("pageSize"));
        assertEquals(41, result.getIntValue("rowNum"));
        assertEquals(5, result.getIntValue("pageCount"));
        assertSame(datasourceList, result.get("tbodyList"));
        assertTrue(result.containsKey("theadList"));
        assertFalse(result.containsKey("Return"));
    }

    /** 未提供回显参数时保留显式 ID 列表，不凭空添加启用状态过滤。 */
    @Test
    public void shouldPreserveIdListAndUnfilteredActiveState() {
        service.listDatasource(JSON.parseObject("{\"idList\":[10]}"));
        assertEquals(Collections.singletonList(10L), listQuery.getIdList());
        assertNull(listQuery.getIsActive());
    }

    /** 空列表保留原分页结构，且不额外查询总数。 */
    @Test
    public void shouldPreserveEmptyListResponse() {
        datasourceList = new ArrayList<>();
        JSONObject result = service.listDatasource(new JSONObject());
        assertEquals(0, result.getIntValue("rowNum"));
        assertEquals(0, result.getIntValue("pageCount"));
        assertTrue(result.getJSONArray("tbodyList").isEmpty());
        assertEquals(0, listCountQueryCount);
    }

    /** 旧配置缺失、null、空白类型在三个操作中均可继续读取数据仓库。 */
    @Test
    public void shouldDefaultMissingAndBlankTypesForEveryOperation() {
        for (String type : Arrays.asList(null, "", " \t\r\n", "datawarehouse")) {
            JSONObject params = request();
            if (type != null) {
                params.put("datasourceType", type);
            }
            assertSame(datasource, service.getDatasource(params));
            assertNotNull(service.listDatasource(params));
            assertEquals(1, service.searchDatasourceData(params).getIntValue("rowNum"));
            params.put("datasourceType", null);
            assertSame(datasource, service.getDatasource(params));
            assertNotNull(service.listDatasource(params));
            assertEquals(1, service.searchDatasourceData(params).getIntValue("rowNum"));
        }
    }

    /** integration 等非空未知类型在访问 Mapper 前失败，不误查同 ID 数据仓库。 */
    @Test
    public void shouldRejectUnsupportedTypesBeforeAnyQuery() {
        for (String type : Arrays.asList("integration", "unknown", "DATAWAREHOUSE", " datawarehouse ")) {
            JSONObject params = request();
            params.put("datasourceType", type);
            assertThrows(DashboardDatasourceTypeNotSupportedException.class, () -> service.listDatasource(params));
            assertThrows(DashboardDatasourceTypeNotSupportedException.class, () -> service.getDatasource(params));
            assertThrows(DashboardDatasourceTypeNotSupportedException.class, () -> service.searchDatasourceData(params));
        }
        assertEquals(0, queryCount);
    }

    /** 详情返回同一数据源及字段对象，不改变 ID 参数，也不包装为表格。 */
    @Test
    public void shouldPreserveDetailAndMissingSourceSemantics() {
        assertSame(datasource, service.getDatasource(request()));
        assertEquals(Long.valueOf(10), detailId);
        datasource = null;
        assertNull(service.getDatasource(request()));
        assertThrows(DataSourceIsNotFoundException.class, () -> service.searchDatasourceData(request()));
    }

    /** 取数响应保留业务列别名、系统列、分页元数据和行中的真实值。 */
    @Test
    public void shouldPreserveTableShapeAndRawRows() {
        JSONObject result = service.searchDatasourceData(request());
        JSONArray headers = result.getJSONArray("theadList");
        assertEquals(4, headers.size());
        assertEquals("field_101", headers.getJSONObject(0).getString("key"));
        assertEquals("jobName", headers.getJSONObject(0).getString("name"));
        assertEquals("作业名", headers.getJSONObject(0).getString("title"));
        assertEquals("field_102", headers.getJSONObject(1).getString("key"));
        assertEquals("duration", headers.getJSONObject(1).getString("name"));
        assertEquals("insertTime", headers.getJSONObject(2).getString("key"));
        assertEquals("expireTime", headers.getJSONObject(3).getString("key"));
        assertEquals("time", headers.getJSONObject(2).getString("type"));
        assertFalse(headers.getJSONObject(2).containsKey("name"));
        assertSame(rows, result.get("tbodyList"));
        assertSame(datasource.getFieldList(), dataQuery.getFieldList());
        assertEquals(1, result.getIntValue("rowNum"));
        assertEquals(1, result.getIntValue("currentPage"));
        assertEquals(20, result.getIntValue("pageSize"));
        assertFalse(result.containsKey("Return"));
    }

    /** limit 缺省/0/正数原样进入 Mapper，同时保留多字段顺序、过滤及过期配置。 */
    @Test
    public void shouldForwardLimitFilteringAndSortPriority() {
        for (Integer limit : Arrays.asList(null, 0, 5)) {
            JSONObject params = request();
            params.put("currentPage", 3);
            params.put("pageSize", 10);
            params.put("isExpired", 1);
            params.put("sortList", JSON.parseArray("[{\"fieldId\":102,\"type\":\"DESC\"},{\"fieldId\":101,\"type\":\"ASC\"}]"));
            params.put("conditionList", JSON.parseArray("[{\"id\":101,\"inputType\":\"text\",\"value\":\"应用一\"}]"));
            if (limit != null) {
                params.put("limit", limit);
            }
            service.searchDatasourceData(params);
            assertEquals(limit, dataQuery.getLimit());
            assertEquals(Integer.valueOf(20), dataQuery.getStartNum());
            assertEquals(Integer.valueOf(1), dataQuery.getIsExpired());
            assertEquals(Long.valueOf(102), dataQuery.getSortList().get(0).getFieldId());
            assertEquals("DESC", dataQuery.getSortList().get(0).getType());
            assertEquals(Long.valueOf(101), dataQuery.getSortList().get(1).getFieldId());
            assertEquals("ASC", dataQuery.getSortList().get(1).getType());
            assertEquals("应用一", dataQuery.getConditionList().get(0).getValue());
        }
    }

    /** 存在字段但无数据时仍返回表头；没有字段时沿用原接口空对象响应。 */
    @Test
    public void shouldDistinguishNoRowsFromNoFields() {
        rows = new ArrayList<>();
        JSONObject result = service.searchDatasourceData(request());
        assertEquals(0, result.getIntValue("rowNum"));
        assertEquals(4, result.getJSONArray("theadList").size());
        assertTrue(result.getJSONArray("tbodyList").isEmpty());
        datasource.setFieldList(Collections.emptyList());
        dataQuery = null;
        assertTrue(service.searchDatasourceData(request()).isEmpty());
        assertNull(dataQuery);
    }

    /** 生成两个旧接口各自使用的 ID 参数，不引入新的统一别名。 */
    private JSONObject request() {
        return JSON.parseObject("{\"id\":10,\"dataSourceId\":10}");
    }

    /** 通过反射注入 Mapper 代理，保持生产服务的原有字段注入风格。 */
    private void inject(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
