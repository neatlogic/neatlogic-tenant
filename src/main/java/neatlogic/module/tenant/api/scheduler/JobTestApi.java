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
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.scheduler.dto.JobVo;
import neatlogic.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import neatlogic.framework.scheduler.exception.ScheduleJobNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

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
            @Param(name = "jobUuid", type = ApiParamType.STRING, desc = "nmtas.jobtestapi.input.param.desc.jobid", isRequired = true),
            @Param(name = "jobHandlerClassName", type = ApiParamType.STRING, desc = "nmtas.jobtestapi.input.param.desc.jobhandlerclassname", isRequired = true)
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String jobUuid = paramObj.getString("jobUuid");
        String jobHandlerClassName = paramObj.getString("jobHandlerClassName");
        IJob jobHandler = SchedulerManager.getHandler(jobHandlerClassName);
        if (jobHandler == null) {
            throw new ScheduleHandlerNotFoundException(jobHandlerClassName);
        }
        String tenantUuid = TenantContext.get().getTenantUuid();
        JobVo jobVo = jobHandler.getJob(jobUuid);
        if (jobVo == null) {
            throw new ScheduleJobNotFoundException(jobUuid);
        }
        if (Objects.equals(jobVo.getIsActive(), 1)) {
            throw new ApiRuntimeException("状态为‘禁用’，才能执行测试");
        }
        JobObject jobObject = new JobObject.Builder(jobVo.getUuid(), jobHandler.getGroupName(), jobHandler.getClassName(), tenantUuid)
                .withRepeatCount(1)
                .needAudit(1)
                .withPropList(jobVo.getPropList())
                .setIsTest(1)
                .setType(jobHandler.getType()).build();
        schedulerManager.loadJob(jobObject);
        return null;
    }

    @Override
    public String getToken() {
        return "job/test";
    }
}
