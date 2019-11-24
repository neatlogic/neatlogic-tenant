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
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobVo;
import codedriver.framework.scheduler.exception.SchedulerExceptionMessage;
import codedriver.framework.scheduler.service.SchedulerService;
@Service
@Transactional
public class DeleteJobByIdApi extends ApiComponentBase {

	private Logger logger = LoggerFactory.getLogger(DeleteJobByIdApi.class);
	
	@Autowired
	private SchedulerService schedulerService;
	
	@Autowired
	private SchedulerMapper schedulerMapper;
	
	@Override
	public String getToken() {
		return "deleteJobByIdApi";
	}

	@Override
	public String getName() {
		return "删除定时作业";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name="jobId",type="Long",isRequired="true",desc="定时作业id")})
	@Description(desc="删除定时作业")
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
		schedulerService.deleteJob(jobId);				
		return "OK";
	}

}
