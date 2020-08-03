package codedriver.module.tenant.api.scheduler;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.framework.scheduler.dto.JobVo;
import codedriver.framework.scheduler.exception.ScheduleJobNotFoundException;

@Service
@AuthAction(name = "SCHEDULE_JOB_MODIFY")
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class JobDeleteApi extends ApiComponentBase {

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

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "定时作业uuid") })
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
		JobObject jobObject = new JobObject.Builder(jobUuid, jobHandler.getGroupName(), jobHandler.getClassName(), tenantUuid).build();
		schedulerManager.unloadJob(jobObject);
		schedulerMapper.deleteJobAuditByJobUuid(jobUuid);
		schedulerMapper.deleteJobPropByJobUuid(jobUuid);
		schedulerMapper.deleteJobByUuid(jobUuid);
		return null;
	}

}
