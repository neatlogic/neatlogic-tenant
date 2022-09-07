/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.datawarehouse;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.DATA_WAREHOUSE_BASE;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceDataMapper;
import codedriver.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceMapper;
import codedriver.framework.datawarehouse.dto.DataSourceDataVo;
import codedriver.framework.datawarehouse.dto.DataSourceFieldVo;
import codedriver.framework.datawarehouse.dto.DataSourceVo;
import codedriver.framework.datawarehouse.exceptions.DataSourceIsNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Service
@AuthAction(action = DATA_WAREHOUSE_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchDataSourceDataApi extends PrivateApiComponentBase {

    @Resource
    private DataWarehouseDataSourceMapper reportDataSourceMapper;
    @Resource
    private DataWarehouseDataSourceDataMapper reportDataSourceDataMapper;


    @Override
    public String getToken() {
        return "datawarehouse/datasource/data/search";
    }

    @Override
    public String getName() {
        return "查询数据源数据";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "dataSourceId", type = ApiParamType.LONG, desc = "数据源id", isRequired = true),
            @Param(name = "conditionList", type = ApiParamType.JSONARRAY, desc = "条件列表"),
            @Param(name = "sortList", type = ApiParamType.JSONARRAY, desc = "排序"),
            @Param(name = "limit", type = ApiParamType.INTEGER, desc = "返回数据限制"),
            @Param(name = "isExpired", type = ApiParamType.INTEGER, desc = "是否过期，0未过期，1已过期")})
    @Output({@Param(explode = BasePageVo.class)})
    @Description(desc = "查询数据源数据接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        DataSourceDataVo reportDataSourceDataVo = JSONObject.toJavaObject(jsonObj, DataSourceDataVo.class);
        //去掉没有值的条件
        //reportDataSourceDataVo.getConditionList().removeIf(d -> d.getValue() == null || StringUtils.isBlank(d.getValue().toString()));
        DataSourceVo reportDataSourceVo = reportDataSourceMapper.getDataSourceById(reportDataSourceDataVo.getDataSourceId());
        if (reportDataSourceVo == null) {
            throw new DataSourceIsNotFoundException(reportDataSourceDataVo.getDataSourceId());
        }
        JSONObject returnObj = new JSONObject();
        if (CollectionUtils.isNotEmpty(reportDataSourceVo.getFieldList())) {
            reportDataSourceDataVo.setFieldList(reportDataSourceVo.getFieldList());
            int rowNum = reportDataSourceDataMapper.searchDataSourceDataCount(reportDataSourceDataVo);
            reportDataSourceDataVo.setRowNum(rowNum);
            List<HashMap<String, Object>> resultList = reportDataSourceDataMapper.searchDataSourceData(reportDataSourceDataVo);

            JSONArray headerList = new JSONArray();
           /* JSONObject idHeadObj = new JSONObject();
            idHeadObj.put("key", "id");
            idHeadObj.put("title", "#");
            headerList.add(idHeadObj);*/

            for (DataSourceFieldVo fieldVo : reportDataSourceVo.getFieldList()) {
                JSONObject headObj = new JSONObject();
                headObj.put("key", "field_" + fieldVo.getId());
                headObj.put("name", fieldVo.getName());
                headObj.put("title", fieldVo.getLabel());
                headerList.add(headObj);
            }

            JSONObject insertTimeHeadObj = new JSONObject();
            insertTimeHeadObj.put("key", "insertTime");
            insertTimeHeadObj.put("title", "同步时间");
            insertTimeHeadObj.put("type", "time");
            headerList.add(insertTimeHeadObj);

            JSONObject expiredTimeHeadObj = new JSONObject();
            expiredTimeHeadObj.put("key", "expireTime");
            expiredTimeHeadObj.put("title", "过期时间");
            expiredTimeHeadObj.put("type", "time");
            headerList.add(expiredTimeHeadObj);

            returnObj.put("currentPage", reportDataSourceDataVo.getCurrentPage());
            returnObj.put("pageSize", reportDataSourceDataVo.getPageSize());
            returnObj.put("pageCount", reportDataSourceDataVo.getPageCount());
            returnObj.put("rowNum", reportDataSourceDataVo.getRowNum());
            returnObj.put("theadList", headerList);
            returnObj.put("tbodyList", resultList);
        }
        return returnObj;
    }

}
