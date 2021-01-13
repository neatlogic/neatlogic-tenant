package codedriver.module.tenant.api.notify.job;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.notify.dao.mapper.NotifyJobMapper;
import codedriver.framework.notify.dto.job.NotifyJobVo;
import codedriver.framework.notify.exception.NotifyJobNotFoundException;
import codedriver.framework.notify.schedule.plugin.NotifyContentJob;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.module.tenant.auth.label.NOTIFY_JOB_MODIFY;
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
