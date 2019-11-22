package codedriver.framework.tenant.api.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.exception.ApiRuntimeException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Example;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.framework.scheduler.dto.JobVo;
import codedriver.framework.scheduler.exception.SchedulerExceptionMessage;
@Service
@Transactional
public class ResumeJobByIdApi extends ApiComponentBase {

	Logger logger = LoggerFactory.getLogger(ResumeJobByIdApi.class);
	
	@Autowired
	private SchedulerManager schedulerManager;
	
	@Autowired
	private SchedulerMapper schedulerMapper;
	
	@Override
	public String getToken() {
		return "resumeJobByIdApi";
	}

	@Override
	public String getName() {
		return "恢复定时作业";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({@Param(name="jobId",type="Long",isRequired="true",desc="定时作业id")})
	@Description(desc="恢复定时作业")
	@Example(example="{\"jobId\":1}")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		
		Long jobId = jsonObj.getLong("jobId");
		JobVo job = schedulerMapper.getJobById(jobId);
		if(job == null) {
			SchedulerExceptionMessage message = new SchedulerExceptionMessage("定时作业："+ jobId + " 不存在");
			logger.error(message.toString());
			throw new ApiRuntimeException(message);
		}
		JobVo jobVo = new JobVo();
		jobVo.setId(jobId);
		jobVo.setStatus(JobVo.RUNNING);
		schedulerMapper.updateJobById(jobVo);
		JobObject jobObject = JobObject.buildJobObject(job);
		schedulerManager.loadJob(jobObject);
			
		return "OK";
	}

}
