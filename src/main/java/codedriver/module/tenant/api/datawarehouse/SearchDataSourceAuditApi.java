/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.datawarehouse;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.DATA_WAREHOUSE_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceAuditMapper;
import codedriver.framework.datawarehouse.dto.DataSourceAuditVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = DATA_WAREHOUSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchDataSourceAuditApi extends PrivateApiComponentBase {

    @Resource
    private DataWarehouseDataSourceAuditMapper reportDataSourceAuditMapper;

    @Override
    public String getToken() {
        return "datawarehouse/datasource/audit/search";
    }

    @Override
    public String getName() {
        return "搜索数据源同步审计信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "dataSourceId", type = ApiParamType.LONG, desc = "数据源id", isRequired = true)})
    @Description(desc = "搜索数据源同步审计信息接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        DataSourceAuditVo reportDataSourceAuditVo = JSONObject.toJavaObject(jsonObj, DataSourceAuditVo.class);
        List<DataSourceAuditVo> auditList = reportDataSourceAuditMapper.searchReportDataSourceAudit(reportDataSourceAuditVo);
        if (CollectionUtils.isNotEmpty(auditList)) {
            int rowNum = reportDataSourceAuditMapper.searchReportDataSourceAuditCount(reportDataSourceAuditVo);
            reportDataSourceAuditVo.setRowNum(rowNum);
        }
        return TableResultUtil.getResult(auditList, reportDataSourceAuditVo);
    }

}
