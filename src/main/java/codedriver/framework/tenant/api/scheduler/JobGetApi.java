package codedriver.framework.tenant.api.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.AuthAction;
import codedriver.framework.exception.core.ApiRuntimeException;
import codedriver.framework.exception.core.FrameworkExceptionMessageBase;
import codedriver.framework.exception.core.IApiExceptionMessage;
import codedriver.framework.exception.type.CustomExceptionMessage;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Example;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobVo;
import codedriver.framework.scheduler.exception.SchedulerExceptionMessage;
@Service
@Transactional
@AuthAction(name="SYSTEM_JOB_EDIT")
public class JobGetApi extends ApiComponentBase {

	Logger logger = LoggerFactory.getLogger(JobGetApi.class);
	
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

	@Input({@Param(name="jobUuid",type=ApiParamType.STRING,isRequired=true,desc="定时作业uuid")})
	@Description(desc="获取定时作业信息")
	@Example(example="{\"jobUuid\":1}")
	@Output({
		@Param(name="uuid",type=ApiParamType.STRING,isRequired=true,desc="定时作业uuid"),
		@Param(name="name",type=ApiParamType.STRING,isRequired=true,desc="定时作业名称"),
		@Param(name="classpath",type=ApiParamType.STRING,isRequired=true,desc="定时作业组件类路径"),
		@Param(name="beginTime",type=ApiParamType.LONG,isRequired=false,desc="开始时间"),
		@Param(name="endTime",type=ApiParamType.LONG,isRequired=false,desc="结束时间"),
		@Param(name="cron",type=ApiParamType.STRING,isRequired=true,desc="corn表达式"),
		@Param(name="isActive",type=ApiParamType.STRING,isRequired=true,desc="是否激活(no:禁用，yes：激活)"),
		@Param(name="needAudit",type=ApiParamType.STRING,isRequired=true,desc="是否保存执行记录no:不保存，yes:保存"),
		@Param(name="jobStatus.status",type=ApiParamType.STRING,isRequired=false,desc="running:运行中;stop:停止;not_loaded未加载"),
		@Param(name="jobStatus.nextFireTime",type=ApiParamType.LONG,isRequired=false,desc="下一次被唤醒时间"),
		@Param(name="jobStatus.lastFireTime",type=ApiParamType.LONG,isRequired=false,desc="最后一次被唤醒时间"),
		@Param(name="jobStatus.lastFinishTime",type=ApiParamType.LONG,isRequired=false,desc="最后一次完成时间"),
		@Param(name="jobStatus.execCount",type=ApiParamType.INTEGER,isRequired=false,desc="执行次数"),
		@Param(name="propList",type=ApiParamType.JSONARRAY,isRequired=false,desc="属性列表"),
		@Param(name="propList[0].id",type=ApiParamType.LONG,isRequired=true,desc="属性id"),
		@Param(name="propList[0].name",type=ApiParamType.STRING,isRequired=true,desc="属性名"),
		@Param(name="propList[0].value",type=ApiParamType.STRING,isRequired=true,desc="属性值")
		})
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String jobUuid = jsonObj.getString("jobUuid");
		JobVo job = schedulerMapper.getJobByUuid(jobUuid);
		if(job == null) {
			IApiExceptionMessage message = new FrameworkExceptionMessageBase(new SchedulerExceptionMessage(new CustomExceptionMessage("定时作业："+ jobUuid + " 不存在")));
			logger.error(message.toString());
			throw new ApiRuntimeException(message);
		}
		return job;
	}

}
