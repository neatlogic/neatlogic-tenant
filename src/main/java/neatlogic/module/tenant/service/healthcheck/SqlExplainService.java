package neatlogic.module.tenant.service.healthcheck;

import com.alibaba.fastjson.JSONObject;

/** SQL 执行计划查询，按审计记录中的数据源隔离连接。 */
public interface SqlExplainService {
    /** 获取传统计划和可选 JSON 计划，未指定数据源时沿用当前请求路由。 */
    JSONObject getSqlExplain(String sql, String datasourceKey, Integer includePlanJson) throws Exception;
}
