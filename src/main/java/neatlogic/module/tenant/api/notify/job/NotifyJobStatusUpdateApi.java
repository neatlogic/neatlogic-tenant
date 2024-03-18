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

package neatlogic.module.tenant.api.notify.job;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
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
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.auth.label.NOTIFY_JOB_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = NOTIFY_JOB_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class NotifyJobStatusUpdateApi extends PrivateApiComponentBase {

	@Autowired
	private NotifyJobMapper notifyJobMapper;

	@Autowired
	private SchedulerManager schedulerManager;

	@Override
	public String getToken() {
		return "notify/job/status/update";
	}

	@Override
	public String getName() {
		return "修改通知定时任务激活状态";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "定时任务ID"),
			@Param(name = "isActive", type = ApiParamType.ENUM, isRequired = true, rule = "0,1", desc = "是否激活(0:禁用，1：激活)"),
	})
	@Output({})
	@Description(desc = "修改通知定时任务激活状态")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long id = jsonObj.getLong("id");
		Integer isActive = jsonObj.getInteger("isActive");
		NotifyJobVo job = notifyJobMapper.getJobBaseInfoById(id);
		if(job == null){
			throw new NotifyJobNotFoundException(id);
		}else{
			job.setLcu(UserContext.get().getUserUuid());
			job.setIsActive(isActive);
			notifyJobMapper.updateJobStatus(job);
		}
		IJob handler = SchedulerManager.getHandler(NotifyContentJob.class.getName());
		String tenantUuid = TenantContext.get().getTenantUuid();
		JobObject newJobObject = new JobObject.Builder(job.getId().toString(), handler.getGroupName(), handler.getClassName(), tenantUuid).withCron(job.getCron()).addData("notifyContentJobId",job.getId()).build();
		if(job.getIsActive().intValue() == 1){
			schedulerManager.loadJob(newJobObject);
		}else{
			schedulerManager.unloadJob(newJobObject);
		}
		return null;
	}
}
