/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
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
