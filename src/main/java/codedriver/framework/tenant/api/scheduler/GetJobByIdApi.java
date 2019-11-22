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
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobVo;
import codedriver.framework.scheduler.exception.SchedulerExceptionMessage;
@Service
@Transactional
public class GetJobByIdApi extends ApiComponentBase {

	Logger logger = LoggerFactory.getLogger(GetJobByIdApi.class);
	
	@Autowired
	private SchedulerMapper schedulerMapper;
	
	@Override
	public String getToken() {
		return "getJobByIdApi";
	}

	@Override
	public String getName() {
		return "获取定时作业信息";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name="jobId",type="Long",isRequired="true",desc="定时作业id")})
	@Description(desc="获取定时作业信息")
	@Example(example="{\"jobId\":1}")
	@Output({
		@Param(name="id",type="Long",isRequired="true",desc="定时作业id"),
		@Param(name="name",type="String",isRequired="true",desc="定时作业名称"),
		@Param(name="classpath",type="String",isRequired="true",desc="定时作业组件类路径"),
		@Param(name="beginTime",type="Long",isRequired="true",desc="开始时间"),
		@Param(name="endTime",type="Long",isRequired="true",desc="结束时间"),
		@Param(name="triggerType",type="String",isRequired="true",desc="触发器类型"),
		@Param(name="repeat",type="Integer",isRequired="false",desc="重复次数"),
		@Param(name="interval",type="Integer",isRequired="false",desc="间隔时间，单位是秒"),
		@Param(name="cron",type="String",isRequired="false",desc="corn表达式"),
		@Param(name="status",type="String",isRequired="true",desc="running:运行中;stop:停止;not_loaded未加载"),
		@Param(name="nextFireTime",type="Long",isRequired="false",desc="下一次被唤醒时间"),
		@Param(name="pauseTime",type="Long",isRequired="false",desc="暂停时间"),
		@Param(name="lastFireTime",type="Long",isRequired="false",desc="最后一次被唤醒时间"),
		@Param(name="lastFinishTime",type="Long",isRequired="false",desc="最后一次完成时间"),
		@Param(name="execCount",type="Integer",isRequired="false",desc="执行次数"),
		@Param(name="isActive",type="String",isRequired="false",desc="是否激活(no:禁用，yes：激活)"),
		@Param(name="needAudit",type="String",isRequired="false",desc="是否保存执行记录no:不保存，yes:保存"),
		@Param(name="serverId",type="Integer",isRequired="false",desc="server_id，一台主机一个serverid"),
		@Param(name="propList",type="Array",isRequired="false",desc="属性列表"),
		@Param(name="propList[0].id",type="Long",isRequired="false",desc="属性id"),
		@Param(name="propList[0].name",type="String",isRequired="false",desc="属性名"),
		@Param(name="propList[0].value",type="String",isRequired="false",desc="属性值")
		})
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long jobId = jsonObj.getLong("jobId");
		JobVo job = schedulerMapper.getJobById(jobId);
		if(job == null) {
			SchedulerExceptionMessage message = new SchedulerExceptionMessage("定时作业："+ jobId + " 不存在");
			logger.error(message.toString());
			throw new ApiRuntimeException(message);
		}
		return job;
	}

}
