package neatlogic.module.tenant.service.healthcheck;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.dao.mapper.healthcheck.SqlStatusMapper;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.store.mysql.DatabaseVendor;
import neatlogic.framework.store.mysql.DatasourceManager;
import neatlogic.framework.store.mysql.NeatLogicBasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 在原始执行库中查询预估计划，避免改变请求和外层事务的数据源。 */
@Service
public class SqlExplainServiceImpl implements SqlExplainService {
    private static final Logger logger = LoggerFactory.getLogger(SqlExplainServiceImpl.class);

    @Resource
    private SqlStatusMapper sqlStatusMapper;

    // 必须使用包含框架 SqlStatusMapper 的根工厂，不能使用 tenant 中的同名工厂。
    @Resource(name = "frameworkSqlSessionFactory")
    private SqlSessionFactory frameworkSqlSessionFactory;

    /** 指定数据源时独立借用连接，普通计划和 JSON 计划使用同一连接。 */
    @Override
    public JSONObject getSqlExplain(String sql, String datasourceKey, Integer includePlanJson) throws Exception {
        if (StringUtils.isBlank(datasourceKey)) {
            // 旧调用继续走 Spring Mapper，不改变已绑定事务及当前请求的行为。
            String databaseName = DatasourceManager.getDatasourceDatabaseName(DatasourceManager.getCurrentDatasourceKey());
            return queryPlan(sqlStatusMapper, sql, includePlanJson, databaseName);
        }
        NeatLogicBasicDataSource datasource = DatasourceManager.getDatasource(datasourceKey);
        if (datasource == null || datasource.isClosed()) {
            throw new ApiRuntimeException("nmtah.getsqlexplainapi.datasourcenotfound", datasourceKey);
        }
        try (Connection connection = datasource.getConnection()) {
            // 这条连接独立于 Spring 事务，仅执行 EXPLAIN，不执行被分析的 SQL。
            connection.setAutoCommit(true);
            String databaseName = connection.getCatalog();
            if (StringUtils.isBlank(databaseName)) {
                databaseName = DatasourceManager.getDatasourceDatabaseName(datasourceKey);
            }
            Configuration configuration = frameworkSqlSessionFactory.getConfiguration();
            // SpringManagedTransactionFactory 不支持 openSession(Connection)，显式使用独立 JDBC 事务。
            try (SqlSession session = new DefaultSqlSession(configuration,
                    configuration.newExecutor(new JdbcTransaction(connection), ExecutorType.SIMPLE), true)) {
                return queryPlan(session.getMapper(SqlStatusMapper.class), sql, includePlanJson, databaseName);
            }
        }
    }

    /** 统一组装兼容旧页面的表格响应，仅 MySQL 按需获取 JSON。 */
    private JSONObject queryPlan(SqlStatusMapper mapper, String sql, Integer includePlanJson, String databaseName) {
        JSONObject resultObj = new JSONObject();
        resultObj.put("sql", sql);
        resultObj.put("databaseName", databaseName);
        resultObj.put("currentPage", 1);
        resultObj.put("pageCount", 1);
        int rowNum = 0;
        int pageSize = 0;
        if (StringUtils.isNotBlank(sql)) {
            List<Map<String, Object>> rows = mapper.selectListBySql("EXPLAIN " + sql);
            resultObj.put("tbodyList", rows);
            rowNum = rows.size();
            pageSize = (rowNum / 20 + 1) * 20;
            boolean isMysql = Objects.equals(DatasourceManager.getDatabaseId(), DatabaseVendor.MYSQL.getDatabaseId());
            resultObj.put("theadList", getHeaders(rows, isMysql));
            if (isMysql && Objects.equals(includePlanJson, 1)) {
                appendPlanJson(mapper, resultObj, sql);
            }
        }
        resultObj.put("pageSize", pageSize);
        resultObj.put("rowNum", rowNum);
        return resultObj;
    }

    /** MySQL 保留固定列，其余数据库继续从结果中提取列名。 */
    private JSONArray getHeaders(List<Map<String, Object>> rows, boolean isMysql) {
        List<String> keys = new ArrayList<>();
        if (isMysql) {
            String[] mysqlKeys = {"id", "select_type", "table", "partitions", "type", "possible_keys", "key", "key_len", "ref", "rows", "filtered", "Extra"};
            for (String key : mysqlKeys) {
                keys.add(key);
            }
        } else {
            for (Map<String, Object> row : rows) {
                for (String key : row.keySet()) {
                    if (!keys.contains(key)) {
                        keys.add(key);
                    }
                }
            }
        }
        JSONArray headers = new JSONArray();
        for (String key : keys) {
            headers.add(new JSONObject().fluentPut("key", key).fluentPut("title", key));
        }
        return headers;
    }

    /** JSON 计划仅用于补充图形分析，查询或解析失败不影响传统明细。 */
    private void appendPlanJson(SqlStatusMapper mapper, JSONObject resultObj, String sql) {
        try {
            List<Map<String, Object>> planRows = mapper.selectListBySql("EXPLAIN FORMAT=JSON " + sql);
            if (planRows == null || planRows.isEmpty() || planRows.get(0) == null) {
                throw new IllegalStateException("JSON 执行计划结果为空");
            }
            Object planValue = null;
            for (Map.Entry<String, Object> entry : planRows.get(0).entrySet()) {
                if ("EXPLAIN".equalsIgnoreCase(entry.getKey())) {
                    planValue = entry.getValue();
                    break;
                }
            }
            if (planValue == null) {
                throw new IllegalStateException("JSON 执行计划缺少 EXPLAIN 列");
            }
            JSONObject planJson = JSONObject.parseObject(planValue.toString());
            if (planJson == null || planJson.isEmpty()) {
                throw new IllegalStateException("JSON 执行计划不是有效对象");
            }
            resultObj.put("planJson", planJson);
        } catch (Exception ex) {
            logger.warn("获取 JSON 执行计划失败，保留传统执行计划", ex);
        }
    }
}
