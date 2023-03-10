/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.datawarehouse;

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
        return "?????????????????????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "dataSourceId", type = ApiParamType.LONG, desc = "?????????id", isRequired = true),
            @Param(name = "conditionList", type = ApiParamType.JSONARRAY, desc = "????????????"),
            @Param(name = "sortList", type = ApiParamType.JSONARRAY, desc = "??????"),
            @Param(name = "limit", type = ApiParamType.INTEGER, desc = "??????????????????"),
            @Param(name = "isExpired", type = ApiParamType.INTEGER, desc = "???????????????0????????????1?????????")})
    @Output({@Param(explode = BasePageVo.class)})
    @Description(desc = "???????????????????????????")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        DataSourceDataVo reportDataSourceDataVo = JSONObject.toJavaObject(jsonObj, DataSourceDataVo.class);
        //????????????????????????
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
            insertTimeHeadObj.put("title", "????????????");
            insertTimeHeadObj.put("type", "time");
            headerList.add(insertTimeHeadObj);

            JSONObject expiredTimeHeadObj = new JSONObject();
            expiredTimeHeadObj.put("key", "expireTime");
            expiredTimeHeadObj.put("title", "????????????");
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
