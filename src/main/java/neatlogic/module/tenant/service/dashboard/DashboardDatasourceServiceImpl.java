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
import neatlogic.framework.util.$;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 大屏数据源类型分发与数据仓库适配。
 * 直接复用公共 Mapper，不调用其他模块的 API，权限由租户模块的大屏 API 统一控制。
 */
@Service
public class DashboardDatasourceServiceImpl implements DashboardDatasourceService {
    private static final String DATAWAREHOUSE = "datawarehouse";

    @Resource
    private DataWarehouseDataSourceMapper dataSourceMapper;
    @Resource
    private DataWarehouseDataSourceDataMapper dataSourceDataMapper;

    /** 列表查询的类型分支集中在租户模块大屏服务，后续数据源不影响前端接口地址。 */
    @Override
    public JSONObject listDatasource(JSONObject params) {
        String datasourceType = resolveDatasourceType(params);
        if (DATAWAREHOUSE.equals(datasourceType)) {
            return listDatawarehouseDatasource(params);
        }
        throw new DashboardDatasourceTypeNotSupportedException(datasourceType);
    }

    /** 详情查询沿用旧接口语义，数据仓库中的 ID 不存在时返回 null。 */
    @Override
    public DataSourceVo getDatasource(JSONObject params) {
        String datasourceType = resolveDatasourceType(params);
        if (DATAWAREHOUSE.equals(datasourceType)) {
            return dataSourceMapper.getDataSourceById(params.getLong("id"));
        }
        throw new DashboardDatasourceTypeNotSupportedException(datasourceType);
    }

    /** 取数查询拒绝未实现的类型，避免集成 ID 被误当成数据仓库 ID。 */
    @Override
    public JSONObject searchDatasourceData(JSONObject params) {
        String datasourceType = resolveDatasourceType(params);
        if (DATAWAREHOUSE.equals(datasourceType)) {
            return searchDatawarehouseData(params);
        }
        throw new DashboardDatasourceTypeNotSupportedException(datasourceType);
    }

    /** 仅缺省、null 和空白类型兼容旧大屏；其他非空值保持原值并严格校验。 */
    private String resolveDatasourceType(JSONObject params) {
        String datasourceType = params.getString("datasourceType");
        return StringUtils.isBlank(datasourceType) ? DATAWAREHOUSE : datasourceType;
    }

    /** 保留原列表接口的精确回显、筛选和分页响应，不增加默认启用状态过滤。 */
    private JSONObject listDatawarehouseDatasource(JSONObject params) {
        DataSourceVo dataSourceVo = JSON.toJavaObject(params, DataSourceVo.class);
        if (CollectionUtils.isNotEmpty(dataSourceVo.getDefaultValue())) {
            List<Long> idList = new ArrayList<>();
            for (int i = 0; i < dataSourceVo.getDefaultValue().size(); i++) {
                idList.add(dataSourceVo.getDefaultValue().getLongValue(i));
            }
            dataSourceVo.setIdList(idList);
        }
        List<DataSourceVo> dataSourceList = dataSourceMapper.searchDataSource(dataSourceVo);
        if (CollectionUtils.isNotEmpty(dataSourceList)) {
            dataSourceVo.setRowNum(dataSourceMapper.searchDataSourceCount(dataSourceVo));
        }
        return TableResultUtil.getResult(dataSourceList, dataSourceVo);
    }

    /**
     * 保留原取数接口的字段别名、元数据和空结果语义。
     * limit 不补默认值：缺省分页、0 不限、正数限行，由既有 MyBatis SQL 处理。
     */
    private JSONObject searchDatawarehouseData(JSONObject params) {
        DataSourceDataVo dataVo = JSON.toJavaObject(params, DataSourceDataVo.class);
        DataSourceVo dataSourceVo = dataSourceMapper.getDataSourceById(dataVo.getDataSourceId());
        if (dataSourceVo == null) {
            throw new DataSourceIsNotFoundException(dataVo.getDataSourceId());
        }
        JSONObject result = new JSONObject();
        if (CollectionUtils.isNotEmpty(dataSourceVo.getFieldList())) {
            dataVo.setFieldList(dataSourceVo.getFieldList());
            dataVo.setRowNum(dataSourceDataMapper.searchDataSourceDataCount(dataVo));
            List<HashMap<String, Object>> resultList = dataSourceDataMapper.searchDataSourceData(dataVo);
            JSONArray headerList = new JSONArray();
            for (DataSourceFieldVo fieldVo : dataSourceVo.getFieldList()) {
                JSONObject head = new JSONObject();
                head.put("key", "field_" + fieldVo.getId());
                head.put("name", fieldVo.getName());
                head.put("title", fieldVo.getLabel());
                headerList.add(head);
            }
            // 系统时间列保持原键名，组件仍通过业务列的 name/field_ID 完成映射。
            JSONObject insertTimeHead = new JSONObject();
            insertTimeHead.put("key", "insertTime");
            insertTimeHead.put("title", $.t("nmtad.searchdatasourcedataapi.header.inserttime"));
            insertTimeHead.put("type", "time");
            headerList.add(insertTimeHead);
            JSONObject expireTimeHead = new JSONObject();
            expireTimeHead.put("key", "expireTime");
            expireTimeHead.put("title", $.t("nmtad.searchdatasourcedataapi.header.expiretime"));
            expireTimeHead.put("type", "time");
            headerList.add(expireTimeHead);
            result.put("currentPage", dataVo.getCurrentPage());
            result.put("pageSize", dataVo.getPageSize());
            result.put("pageCount", dataVo.getPageCount());
            result.put("rowNum", dataVo.getRowNum());
            result.put("theadList", headerList);
            result.put("tbodyList", resultList);
        }
        return result;
    }
}
