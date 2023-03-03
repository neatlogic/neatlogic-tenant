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

package neatlogic.module.tenant.api.notify.job;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.notify.dao.mapper.NotifyJobMapper;
import neatlogic.framework.notify.dto.job.NotifyJobVo;
import neatlogic.framework.notify.exception.NotifyJobNotFoundException;
import neatlogic.module.framework.notify.schedule.handler.NotifyContentJob;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dao.mapper.SchedulerMapper;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.auth.label.NOTIFY_JOB_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AuthAction(action = NOTIFY_JOB_MODIFY.class)
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class NotifyJobDeleteApi extends PrivateApiComponentBase {

	@Autowired
	private NotifyJobMapper notifyJobMapper;

	@Autowired
	private SchedulerMapper schedulerMapper;

	@Autowired
	private SchedulerManager schedulerManager;

	@Override
	public String getToken() {
		return "notify/job/delete";
	}

	@Override
	public String getName() {
		return "删除通知定时任务";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true,desc = "定时任务ID")})
	@Output({})
	@Description(desc = "删除通知定时任务")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long id = jsonObj.getLong("id");
		NotifyJobVo job = notifyJobMapper.getJobBaseInfoById(id);
		if(job == null){
			throw new NotifyJobNotFoundException(id);
		}
		IJob handler = SchedulerManager.getHandler(NotifyContentJob.class.getName());
		String tenantUuid = TenantContext.get().getTenantUuid();
		JobObject newJobObject = new JobObject.Builder(job.getId().toString(), handler.getGroupName(), handler.getClassName(), tenantUuid).withCron(job.getCron()).addData("notifyContentJobId",job.getId()).build();
		schedulerManager.unloadJob(newJobObject);
		schedulerMapper.deleteJobAuditByJobUuid(id.toString());
		notifyJobMapper.deleteJobById(id);
		notifyJobMapper.deleteReceiverByJobId(id);

		return null;
	}
}
