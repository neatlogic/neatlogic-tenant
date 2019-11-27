package codedriver.framework.tenant.api.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.AuthAction;
import codedriver.framework.exception.ApiRuntimeException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Example;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobAuditVo;
import codedriver.framework.scheduler.exception.SchedulerExceptionMessage;
@Service
@Transactional
@AuthAction(name="SYSTEM_JOB_EDIT")
public class JobAuditLogGetApi extends ApiComponentBase {

	private Logger logger = LoggerFactory.getLogger(JobAuditLogGetApi.class);
	
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
	@Input({@Param(name="auditId",type="Long",isRequired="true",desc="定时作业执行记录id")})
	@Description(desc="获取定时作业执行记录日志")
	@Example(example="{\"auditId\":1}")
	@Output({@Param(name="logContent",type="String",isRequired="true",desc="日志内容")})
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long auditId = jsonObj.getLong("auditId");
		JobAuditVo jobAudit = schedulerMapper.getJobAuditLogById(auditId);
		if(jobAudit == null) {
			SchedulerExceptionMessage message = new SchedulerExceptionMessage("定时作业执行记录：" + auditId + "不存在");
			logger.error(message.toString());
			throw new ApiRuntimeException(message);
		}
		JSONObject resultObj = new JSONObject();
		resultObj.put("logContent", jobAudit.getLogContent());
		return resultObj;
	}

}
