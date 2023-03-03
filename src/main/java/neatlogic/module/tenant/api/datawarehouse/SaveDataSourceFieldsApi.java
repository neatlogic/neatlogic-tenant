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
import neatlogic.framework.auth.label.DATA_WAREHOUSE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceMapper;
import neatlogic.framework.datawarehouse.dto.DataSourceFieldVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@AuthAction(action = DATA_WAREHOUSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class SaveDataSourceFieldsApi extends PrivateApiComponentBase {

    @Resource
    private DataWarehouseDataSourceMapper dataSourceMapper;

    @Override
    public String getToken() {
        return "datawarehouse/datasource/fields/save";
    }

    @Override
    public String getName() {
        return "保存数据仓库数据源字段设置";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "fieldList", type = ApiParamType.JSONARRAY, desc = "字段列表", isRequired = true)})
    @Description(desc = "保存数据仓库数据源字段设置接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray fieldList = jsonObj.getJSONArray("fieldList");
        if (CollectionUtils.isNotEmpty(fieldList)) {
            for (int i = 0; i < fieldList.size(); i++) {
                DataSourceFieldVo dataSourceFieldVo = JSONObject.toJavaObject(fieldList.getJSONObject(i), DataSourceFieldVo.class);
                if (dataSourceFieldVo.getIsCondition() == 0) {
                    dataSourceFieldVo.setInputType(null);
                    dataSourceFieldVo.setConfig(null);
                }
                dataSourceMapper.updateDataSourceFieldCondition(dataSourceFieldVo);
            }
        }
        return null;

    }

}
