package neatlogic.module.tenant.service.dashboard;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.datawarehouse.dto.DataSourceVo;

/** 大屏统一数据源入口，组件无需感知底层数据源所属模块。 */
public interface DashboardDatasourceService {

    /** 按数据源类型查询列表，保留搜索、分页和选中值回显参数。 */
    JSONObject listDatasource(JSONObject params);

    /** 按数据源类型读取详情及字段定义。 */
    DataSourceVo getDatasource(JSONObject params);

    /** 按数据源类型读取组件数据，保留过滤、排序及行数限制。 */
    JSONObject searchDatasourceData(JSONObject params);
}
