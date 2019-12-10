package codedriver.framework.tenant.api.scheduler;

import java.util.List;

import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Example;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobClassVo;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.framework.scheduler.dto.JobVo;
import codedriver.framework.scheduler.exception.ScheduleIllegalParameterException;
import codedriver.framework.scheduler.exception.ScheduleJobClassNotFoundException;
import codedriver.framework.scheduler.exception.ScheduleJobSingletonException;
import codedriver.framework.scheduler.service.SchedulerService;
@Service
@AuthAction(name="SYSTEM_JOB_EDIT")
public class JobSaveApi extends ApiComponentBase {
	@Autowired
	private SchedulerService schedulerService;
	@Autowired
	private SchedulerMapper schedulerMapper;
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
		@Param(name="uuid",type=ApiParamType.STRING,isRequired=false,desc="定时作业uuid"),
		@Param(name="name",type=ApiParamType.STRING,isRequired=true,desc="定时作业名称"),
		@Param(name="classpath",type=ApiParamType.STRING,isRequired=true,desc="定时作业组件类路径"),
		@Param(name="beginTime",type=ApiParamType.LONG,isRequired=false,desc="开始时间"),
		@Param(name="endTime",type=ApiParamType.LONG,isRequired=false,desc="结束时间"),
		@Param(name="cron",type=ApiParamType.STRING,isRequired=true,desc="corn表达式"),
		@Param(name="isActive",type=ApiParamType.STRING,isRequired=true,desc="是否激活(no:禁用，yes：激活)"),
		@Param(name="needAudit",type=ApiParamType.STRING,isRequired=true,desc="是否保存执行记录(no:不保存，yes:保存)"),
		@Param(name="propList",type=ApiParamType.JSONARRAY,isRequired=false,desc="属性列表"),
		@Param(name="propList[0].name",type=ApiParamType.STRING,isRequired=false,desc="属性名"),
		@Param(name="propList[0].value",type=ApiParamType.STRING,isRequired=false,desc="属性值")
		})
	@Output({
		@Param(name="uuid",type=ApiParamType.STRING,isRequired=true,desc="定时作业uuid")
	})
	@Description(desc="保存定时作业信息")
	@Example(example="{name:\"测试_1\", classpath:\"codedriver.framework.scheduler.core.TestJob\", triggerType:\"simple\", repeat:\"10\", interval:\"60\", isActive:\"no\", needAudit:\"no\", beginTime:1573530069000, propList:[{name:\"p_1\",value:\"1\"},{name:\"p_2\",value:\"2\"},{name:\"p_3\",value:\"3\"},{name:\"p_4\",value:\"4\"},{name:\"p_5\",value:\"5\"}]}")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String classpath = jsonObj.getString("classpath");
		IJob job = SchedulerManager.getInstance(classpath);
		if(job == null) {
			throw new ScheduleJobClassNotFoundException("定时作业组件："+ classpath + " 不存在");
		}
		String isActive = jsonObj.getString("isActive");
		if(!JobVo.YES.equals(isActive) && !JobVo.NO.equals(isActive)) {
			throw new ScheduleIllegalParameterException("isActive参数值必须是'" + JobVo.YES + "'或'" + JobVo.NO + "'");
		}
		String needAudit = jsonObj.getString("needAudit");
		if(!JobVo.YES.equals(needAudit) && !JobVo.NO.equals(needAudit)) {
			throw new ScheduleIllegalParameterException("needAudit参数值必须是'" + JobVo.YES + "'或'" + JobVo.NO + "'");
		}	
		String cron = jsonObj.getString("cron");
		if(!CronExpression.isValidExpression(cron)) {
			throw new ScheduleIllegalParameterException("cron表达式参数格式不正确：" + cron);
		}					
		
		JobVo jobVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<JobVo>() {});
		job.valid(jobVo.getPropList());
		JobClassVo jobClass = SchedulerManager.getJobClassByClasspath(classpath);
		if(jobClass == null) {
			throw new ScheduleJobClassNotFoundException("定时作业组件："+ classpath + " 不存在");
		}
		if(JobClassVo.ONCE_TYPE.equals(jobClass.getType())) {
			List<JobVo> jobList = schedulerMapper.getJobByClasspath(classpath);
			if(jobList.size() > 0) {
				throw new ScheduleJobSingletonException("定时作业组件："+ classpath + " 只能创建一个作业");
			}
		}
		schedulerService.saveJob(jobVo);
		if(JobVo.YES.equals(jobVo.getIsActive())) {
			JobObject jobObject = JobObject.buildJobObject(jobVo, JobObject.FRAMEWORK);
			schedulerManager.loadJob(jobObject);
			schedulerManager.broadcastNewJob(jobObject);			
		}
				
		JSONObject resultObj = new JSONObject();
		resultObj.put("uuid",jobVo.getUuid());
		return resultObj;
	}

}
