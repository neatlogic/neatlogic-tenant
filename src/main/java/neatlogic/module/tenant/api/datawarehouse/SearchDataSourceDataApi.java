/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.datawarehouse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.DATA_WAREHOUSE_BASE;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceDataMapper;
import neatlogic.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceMapper;
import neatlogic.framework.datawarehouse.dto.DataSourceDataVo;
import neatlogic.framework.datawarehouse.dto.DataSourceFieldVo;
import neatlogic.framework.datawarehouse.dto.DataSourceVo;
import neatlogic.framework.datawarehouse.exceptions.DataSourceIsNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
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
    @Description(desc = "查询数据源数据")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        DataSourceDataVo reportDataSourceDataVo = JSON.toJavaObject(jsonObj, DataSourceDataVo.class);
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
