/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.datawarehouse;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.DATA_WAREHOUSE_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceMapper;
import codedriver.framework.datawarehouse.dto.ReportDataSourceVo;
import codedriver.framework.datawarehouse.exceptions.DataSourceIsNotFoundException;
import codedriver.framework.datawarehouse.service.ReportDataSourceService;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@AuthAction(action = DATA_WAREHOUSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
@Transactional
public class DeleteReportDataSourceApi extends PrivateApiComponentBase {

    @Resource
    private DataWarehouseDataSourceMapper reportDataSourceMapper;


    @Resource
    private ReportDataSourceService reportDataSourceService;


    @Override
    public String getToken() {
        return "datawarehouse/datasource/delete";
    }

    @Override
    public String getName() {
        return "删除数据仓库数据源";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "id", isRequired = true)})
    @Description(desc = "删除数据仓库数据源接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        ReportDataSourceVo reportDataSourceVo = reportDataSourceMapper.getReportDataSourceById(id);
        if (reportDataSourceVo == null) {
            throw new DataSourceIsNotFoundException(id);
        }
        reportDataSourceService.deleteReportDataSource(reportDataSourceVo);
        return null;
    }

}
