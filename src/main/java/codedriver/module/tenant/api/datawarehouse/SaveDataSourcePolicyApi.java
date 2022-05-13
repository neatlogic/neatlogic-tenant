/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.datawarehouse;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.DATA_WAREHOUSE_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceMapper;
import codedriver.framework.datawarehouse.dto.DataSourceVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.module.framework.scheduler.datawarehouse.ReportDataSourceJob;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@AuthAction(action = DATA_WAREHOUSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class SaveDataSourcePolicyApi extends PrivateApiComponentBase {
    @Resource
    private SchedulerManager schedulerManager;

    @Resource
    private DataWarehouseDataSourceMapper reportDataSourceMapper;

    @Override
    public String getToken() {
        return "datawarehouse/datasource/policy/save";
    }

    @Override
    public String getName() {
        return "保存数据仓库数据源同步策略";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "id"),
            @Param(name = "label", type = ApiParamType.STRING, desc = "显示名", isRequired = true),
            @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "是否激活", isRequired = true),
            @Param(name = "expireCount", type = ApiParamType.INTEGER, desc = "有效时间数值"),
            @Param(name = "expireUnit", type = ApiParamType.ENUM, rule = "minute,hour,day,month,year", desc = "有效时间单位"),
            @Param(name = "mode", type = ApiParamType.ENUM, desc = "同步模式", rule = "replace,append", isRequired = true),
            @Param(name = "cronExpression", type = ApiParamType.STRING, desc = "定时策略"),
    })
    @Output({@Param(explode = BasePageVo.class), @Param(name = "tbodyList", explode = DataSourceVo[].class)})
    @Description(desc = "保存数据仓库数据源同步策略接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        DataSourceVo reportDataSourceVo = JSONObject.toJavaObject(jsonObj, DataSourceVo.class);
        reportDataSourceMapper.updateDataSourcePolicy(reportDataSourceVo);
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
