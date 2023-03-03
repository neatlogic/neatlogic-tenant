/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.scheduler;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dao.mapper.SchedulerMapper;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.scheduler.dto.JobVo;
import neatlogic.framework.scheduler.exception.ScheduleJobNotFoundException;
import neatlogic.framework.auth.label.SCHEDULE_JOB_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AuthAction(action = SCHEDULE_JOB_MODIFY.class)

@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class JobDeleteApi extends PrivateApiComponentBase {

    @Autowired
    private SchedulerManager schedulerManager;

    @Autowired
    private SchedulerMapper schedulerMapper;

    @Override
    public String getToken() {
        return "job/delete";
    }

    @Override
    public String getName() {
        return "删除定时作业";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "定时作业uuid")})
    @Description(desc = "删除定时作业")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String jobUuid = jsonObj.getString("uuid");
        JobVo job = schedulerMapper.getJobBaseInfoByUuid(jobUuid);
        if (job == null) {
            throw new ScheduleJobNotFoundException(jobUuid);
        }
        String tenantUuid = TenantContext.get().getTenantUuid();
        IJob jobHandler = SchedulerManager.getHandler(job.getHandler());
        if (jobHandler != null) {
            JobObject jobObject = new JobObject.Builder(jobUuid, jobHandler.getGroupName(), jobHandler.getClassName(), tenantUuid).build();
            schedulerManager.unloadJob(jobObject);
        }
        schedulerMapper.deleteJobAuditByJobUuid(jobUuid);
        schedulerMapper.deleteJobPropByJobUuid(jobUuid);
        schedulerMapper.deleteJobByUuid(jobUuid);
        return null;
    }

}
