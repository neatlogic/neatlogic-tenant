package codedriver.framework.tenant.api.scheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.AuthAction;
import codedriver.framework.exception.ApiRuntimeException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Example;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.framework.scheduler.dto.JobPropVo;
import codedriver.framework.scheduler.dto.JobVo;
import codedriver.framework.scheduler.exception.SchedulerExceptionMessage;
import codedriver.framework.scheduler.service.SchedulerService;
@Service
@AuthAction(name="SYSTEM_JOB_EDIT")
public class JobSaveApi extends ApiComponentBase {

	private Logger logger = LoggerFactory.getLogger(JobSaveApi.class);
	@Autowired
	private SchedulerService schedulerService;
	@Autowired
	private SchedulerManager schedulerManager;
	@Override
	public String getToken() {
		return "job/save";
	}

	@Override
	public String getName() {
		return "保存定时作业信息";
	}

	@Override
	public String getConfig() {
		return null;
	}

	
	@Input({
		@Param(name="uuid",type="String",isRequired="false",desc="定时作业uuid"),
		@Param(name="name",type="String",isRequired="true",desc="定时作业名称"),
		@Param(name="classpath",type="String",isRequired="true",desc="定时作业组件类路径"),
		@Param(name="beginTime",type="Long",isRequired="false",desc="开始时间"),
		@Param(name="endTime",type="Long",isRequired="false",desc="结束时间"),
		@Param(name="triggerType",type="String",isRequired="true",desc="触发器类型simple或cron"),
		@Param(name="repeat",type="Integer",isRequired="false",desc="重复次数"),
		@Param(name="interval",type="Integer",isRequired="false",desc="间隔时间，单位是秒"),
		@Param(name="cron",type="String",isRequired="false",desc="corn表达式"),
		@Param(name="isActive",type="String",isRequired="false",desc="是否激活(no:禁用，yes：激活)"),
		@Param(name="needAudit",type="String",isRequired="false",desc="是否保存执行记录(no:不保存，yes:保存)"),
		@Param(name="propList",type="Array",isRequired="false",desc="属性列表"),
		@Param(name="propList[0].name",type="String",isRequired="false",desc="属性名"),
		@Param(name="propList[0].value",type="String",isRequired="false",desc="属性值")
		})
	@Description(desc="保存定时作业信息")
	@Example(example="{name:\"测试_1\", classpath:\"codedriver.framework.scheduler.core.TestJob\", triggerType:\"simple\", repeat:\"10\", interval:\"60\", isActive:\"no\", needAudit:\"no\", beginTime:1573530069000, propList:[{name:\"p_1\",value:\"1\"},{name:\"p_2\",value:\"2\"},{name:\"p_3\",value:\"3\"},{name:\"p_4\",value:\"4\"},{name:\"p_5\",value:\"5\"}]}")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JobVo jobVo = new JobVo();
		if(jsonObj.containsKey("uuid")) {
			String uuid = jsonObj.getString("uuid");
			jobVo.setUuid(uuid);
		}
		String name = jsonObj.getString("name");
		jobVo.setName(name);
		String classpath = jsonObj.getString("classpath");
		IJob job = SchedulerManager.getInstance(classpath);
		if(job != null) {
			List<JobPropVo> propList = new ArrayList<>();
			if(jsonObj.containsKey("propList")) {
				JSONArray propArray = jsonObj.getJSONArray("propList");
				if(propArray != null) {
					for(int i = 0; i < propArray.size(); i++) {
						JSONObject prop = propArray.getJSONObject(i);
						propList.add(new JobPropVo(prop.getString("name"), prop.getString("value")));
					}
				}				
			}
			if(job.valid(propList)) {
				jobVo.setPropList(propList);
			}
		}else {
			SchedulerExceptionMessage message = new SchedulerExceptionMessage("定时作业组件："+ classpath + " 不存在");
			logger.error(message.toString());
			throw new ApiRuntimeException(message);
		}
		jobVo.setClasspath(classpath);
		String isActive = jsonObj.getString("isActive");
		if(!JobVo.YES.equals(isActive) && !JobVo.NO.equals(isActive)) {
			SchedulerExceptionMessage message = new SchedulerExceptionMessage("isActive参数值必须是" + JobVo.YES + "或" + JobVo.NO + "'");
			logger.error(message.toString());
			throw new ApiRuntimeException(message);
		}
		jobVo.setIsActive(isActive);
		String needAudit = jsonObj.getString("needAudit");
		if(JobVo.YES.equals(needAudit) || JobVo.NO.equals(needAudit)) {
			jobVo.setNeedAudit(needAudit);
		}else {
			SchedulerExceptionMessage message = new SchedulerExceptionMessage("needAudit参数值必须是'" + JobVo.YES + "'或'" + JobVo.NO + "'");
			logger.error(message.toString());
			throw new ApiRuntimeException(message);
		}
		
		
		String cron = jsonObj.getString("cron");
		if(CronExpression.isValidExpression(cron)) {
			jobVo.setCron(cron);
		}else {
			SchedulerExceptionMessage message = new SchedulerExceptionMessage("cron表达式参数格式不正确");
			logger.error(message.toString());
			throw new ApiRuntimeException(message);
		}				
		
		
		if(jsonObj.containsKey("beginTime")) {
			Long beginTime = jsonObj.getLong("beginTime");
			jobVo.setBeginTime(new Date(beginTime));
		}
		if(jsonObj.containsKey("endTime")) {
			Long endTime = jsonObj.getLong("endTime");
			jobVo.setEndTime(new Date(endTime));
		}
		schedulerService.saveJob(jobVo);
		if(JobVo.YES.equals(jobVo.getIsActive())) {
			JobObject jobObject = JobObject.buildJobObject(jobVo, JobObject.FRAMEWORK);
			schedulerManager.loadJob(jobObject);				
		}
				
		JSONObject resultObj = new JSONObject();
		resultObj.put("id",jobVo.getUuid());
		return resultObj;
	}

}
