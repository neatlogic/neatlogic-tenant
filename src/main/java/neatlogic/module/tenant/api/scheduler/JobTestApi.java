/*
 * Copyright (C) 2025  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.tenant.api.scheduler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.SCHEDULE_JOB_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@AuthAction(action = SCHEDULE_JOB_MODIFY.class)
@Service
public class JobTestApi extends PrivateApiComponentBase {
    @Resource
    private SchedulerManager schedulerManager;

    @Override
    public String getName() {
        return null;
    }

    @Input({
            @Param(name = "jobId", type = ApiParamType.STRING, desc = "nmtas.jobtestapi.input.param.desc.jobid", isRequired = true),
            @Param(name = "jobGroup", type = ApiParamType.STRING, desc = "nmtas.jobtestapi.input.param.desc.jobgroup", isRequired = true),
            @Param(name = "jobHandlerClassName", type = ApiParamType.STRING, desc = "nmtas.jobtestapi.input.param.desc.jobhandlerclassname", isRequired = true),
            @Param(name = "type", type = ApiParamType.STRING, rule = "private,public", desc = "nmtas.jobtestapi.input.param.desc.type"),
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String jobId = paramObj.getString("jobId");
        String jobGroup = paramObj.getString("jobGroup");
        String jobHandlerClassName = paramObj.getString("jobHandlerClassName");
        String type = "private";
        if(StringUtils.isNotBlank(paramObj.getString("type"))) {
            type = paramObj.getString("type");
        }
        IJob jobHandler = SchedulerManager.getHandler(jobHandlerClassName);
        if (jobHandler == null) {
            throw new ScheduleHandlerNotFoundException(jobHandlerClassName);
        }
        String tenantUuid = TenantContext.get().getTenantUuid();
        JobObject jobObject = new JobObject.Builder("TEST@" + jobId + "@TEST", jobGroup, jobHandlerClassName, tenantUuid).withRepeatCount(1)
                .setType(type)
                .build();
        schedulerManager.loadJob(jobObject);
        return null;
    }

    @Override
    public String getToken() {
        return "job/test";
    }
}
