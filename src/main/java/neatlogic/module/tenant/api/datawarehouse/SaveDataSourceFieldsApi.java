/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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
