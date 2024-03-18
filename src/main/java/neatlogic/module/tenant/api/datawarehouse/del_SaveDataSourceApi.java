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

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.DATA_WAREHOUSE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceMapper;
import neatlogic.framework.datawarehouse.dto.DataSourceVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.module.framework.scheduler.datawarehouse.ReportDataSourceJob;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@AuthAction(action = DATA_WAREHOUSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class del_SaveDataSourceApi extends PrivateApiComponentBase {
    @Resource
    private SchedulerManager schedulerManager;

    @Resource
    private DataWarehouseDataSourceMapper reportDataSourceMapper;

    @Override
    public String getToken() {
        return "datawarehouse/datasource/save/del";
    }

    @Override
    public String getName() {
        return "保存数据仓库数据源";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "id，不存在代表添加"),
            @Param(name = "label", type = ApiParamType.STRING, desc = "名称", maxLength = 50, isRequired = true, xss = true),
            @Param(name = "description", type = ApiParamType.STRING, desc = "说明", xss = true, maxLength = 500),
            @Param(name = "conditionList", type = ApiParamType.JSONARRAY, desc = "条件列表"),
            @Param(name = "cronExpression", type = ApiParamType.STRING, desc = "定时策略")})
    @Output({@Param(explode = BasePageVo.class), @Param(name = "tbodyList", explode = DataSourceVo[].class)})
    @Description(desc = "保存数据仓库数据源接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        DataSourceVo reportDataSourceVo = JSONObject.toJavaObject(jsonObj, DataSourceVo.class);
        reportDataSourceMapper.updateDataSource(reportDataSourceVo);
       /* if (CollectionUtils.isNotEmpty(reportDataSourceVo.getConditionList())) {
            for (DataSourceConditionVo condition : reportDataSourceVo.getConditionList()) {
                if (condition.getIsRequired() == 1 && StringUtils.isBlank(condition.getValue())) {
                    throw new DataSourceConditionRequiredValueIsEmptyException(condition);
                }
                reportDataSourceMapper.updateReportDataSourceConditionValue(condition);
            }
        }*/

        String tenantUuid = TenantContext.get().getTenantUuid();
        IJob jobHandler = SchedulerManager.getHandler(ReportDataSourceJob.class.getName());
        JobObject jobObject = new JobObject.Builder(reportDataSourceVo.getId().toString(), jobHandler.getGroupName(), jobHandler.getClassName(), tenantUuid)
                .withCron(reportDataSourceVo.getCronExpression())
                .addData("datasourceId", reportDataSourceVo.getId())
                .build();
        if (StringUtils.isNotBlank(reportDataSourceVo.getCronExpression())) {
            schedulerManager.loadJob(jobObject);
        } else {
            schedulerManager.unloadJob(jobObject);
        }
        return null;
    }

}
