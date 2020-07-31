package codedriver.module.tenant.api.scheduler;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobPropVo;
import codedriver.framework.scheduler.dto.JobVo;
import codedriver.framework.scheduler.exception.ScheduleJobNotFoundException;
@Service
@AuthAction(name="SCHEDULE_JOB_MODIFY")
@OperationType(type = OperationTypeEnum.SEARCH)
public class JobGetApi extends ApiComponentBase {
	
	@Autowired
	private SchedulerMapper schedulerMapper;
	
	@Override
	public String getToken() {
		return "job/get";
	}

	@Override
	public String getName() {
		return "获取定时作业信息";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name="uuid",type=ApiParamType.STRING,isRequired=true,desc="定时作业uuid")})
	@Description(desc="获取定时作业信息")
	@Output({
		@Param(name="Return",explode=JobVo.class,desc="定时作业信息"),
		@Param(name="propList",explode=JobPropVo[].class,desc="属性列表")
		})
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		JobVo job = schedulerMapper.getJobByUuid(uuid);
		if(job == null) {
			throw new ScheduleJobNotFoundException(uuid);
		}
		return job;
	}

}
