package codedriver.framework.tenant.api.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Example;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.framework.scheduler.dto.JobVo;
import codedriver.framework.scheduler.exception.ScheduleJobNotFoundException;
@Service
@AuthAction(name="SYSTEM_JOB_EDIT")
public class JobResumeApi extends ApiComponentBase {
	
	@Autowired
	private SchedulerManager schedulerManager;
	@Autowired
	private SchedulerMapper schedulerMapper;
	
	@Override
	public String getToken() {
		return "job/resume";
	}

	@Override
	public String getName() {
		return "恢复定时作业";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({@Param(name="jobUuid",type=ApiParamType.STRING,isRequired=true,desc="定时作业id")})
	@Description(desc="恢复定时作业")
	@Example(example="{\"jobUuid\":1}")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {		
		String jobUuid = jsonObj.getString("jobUuid");
		JobVo job = schedulerMapper.getJobByUuid(jobUuid);
		if(job == null) {
			throw new ScheduleJobNotFoundException(jobUuid);
		}
		JobObject jobObject = JobObject.buildJobObject(job, JobObject.FRAMEWORK);
		schedulerManager.loadJob(jobObject);
		
		return null;
	}

}