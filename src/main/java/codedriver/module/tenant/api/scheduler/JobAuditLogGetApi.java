package codedriver.module.tenant.api.scheduler;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobAuditVo;
import codedriver.framework.scheduler.exception.ScheduleJobAuditNotFoundException;
import codedriver.module.tenant.auth.label.SCHEDULE_JOB_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = SCHEDULE_JOB_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class JobAuditLogGetApi extends PrivateApiComponentBase {

	@Autowired
	private SchedulerMapper schedulerMapper;

	@Override
	public String getToken() {
		return "job/audit/log/get";
	}

	@Override
	public String getName() {
		return "获取定时作业执行记录日志";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "auditId", type = ApiParamType.LONG, isRequired = true, desc = "定时作业执行记录id") })
	@Output({ @Param(name = "Return", type = ApiParamType.STRING, isRequired = true, desc = "日志内容") })
	@Description(desc = "获取定时作业执行记录日志")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long auditId = jsonObj.getLong("auditId");
		JobAuditVo jobAudit = schedulerMapper.getJobAuditById(auditId);
		if (jobAudit == null) {
			throw new ScheduleJobAuditNotFoundException(auditId);
		}
		return jobAudit.getContent();
	}

}
