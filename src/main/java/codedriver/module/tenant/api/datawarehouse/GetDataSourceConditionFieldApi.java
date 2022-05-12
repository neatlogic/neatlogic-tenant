/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.datawarehouse;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.DATA_WAREHOUSE_BASE;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceMapper;
import codedriver.framework.datawarehouse.dto.DataSourceFieldVo;
import codedriver.framework.datawarehouse.dto.DataSourceVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.stream.Collectors;

@Service
@AuthAction(action = DATA_WAREHOUSE_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetDataSourceConditionFieldApi extends PrivateApiComponentBase {

    @Resource
    private DataWarehouseDataSourceMapper dataSourceMapper;

    @Override
    public String getToken() {
        return "datawarehouse/datasource/condition/get";
    }

    @Override
    public String getName() {
        return "获取数据仓库数据源条件字段";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "数据源id", isRequired = true)})
    @Output({@Param(explode = DataSourceFieldVo[].class)})
    @Description(desc = "获取数据仓库数据源条件字段接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        DataSourceVo dataSourceVo = dataSourceMapper.getDataSourceById(id);
        return dataSourceVo.getFieldList().stream().filter(d -> d.getIsCondition() == 1).collect(Collectors.toList());
    }

}
