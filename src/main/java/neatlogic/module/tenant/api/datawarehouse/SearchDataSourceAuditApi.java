/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.datawarehouse;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.DATA_WAREHOUSE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceAuditMapper;
import neatlogic.framework.datawarehouse.dto.DataSourceAuditVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
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
