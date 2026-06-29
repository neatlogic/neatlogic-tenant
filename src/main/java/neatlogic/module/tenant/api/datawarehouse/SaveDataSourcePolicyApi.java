/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.datawarehouse;

import com.alibaba.fastjson.JSONObject;
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
import neatlogic.framework.scheduler.enums.JobLoadTriggerType;
import neatlogic.module.framework.scheduler.datawarehouse.ReportDataSourceJob;
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
        schedulerManager.saveJobSource(jobObject);
        if (StringUtils.isNotBlank(reportDataSourceVo.getCronExpression())) {
            schedulerManager.loadJob(jobObject, JobLoadTriggerType.SERVER_RESTART);
        } else {
            schedulerManager.unloadJob(jobObject);
        }
        return null;
    }

}
