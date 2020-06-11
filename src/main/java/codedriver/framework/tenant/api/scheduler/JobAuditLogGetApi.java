package codedriver.framework.tenant.api.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobAuditVo;
import codedriver.framework.scheduler.exception.ScheduleJobAuditNotFoundException;

@Service
@AuthAction(name = "SYSTEM_JOB_EDIT")
public class JobAuditLogGetApi extends ApiComponentBase {

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
