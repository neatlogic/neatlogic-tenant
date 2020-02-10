package codedriver.framework.tenant.api.scheduler;

import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Example;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActive;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.framework.scheduler.dto.JobVo;
import codedriver.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import codedriver.framework.scheduler.exception.ScheduleIllegalParameterException;
import codedriver.framework.scheduler.service.SchedulerService;

@Service
@AuthAction(name = "SYSTEM_JOB_EDIT")
@IsActive
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

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, isRequired = false, desc = "定时作业uuid"), @Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "定时作业名称"), @Param(name = "className", type = ApiParamType.STRING, isRequired = true, desc = "定时作业组件类名称"), @Param(name = "beginTime", type = ApiParamType.LONG, isRequired = false, desc = "开始时间"), @Param(name = "endTime", type = ApiParamType.LONG, isRequired = false, desc = "结束时间"),
			@Param(name = "cron", type = ApiParamType.STRING, isRequired = true, desc = "corn表达式"), @Param(name = "isActive", type = ApiParamType.ENUM, isRequired = true, rule = "0,1", desc = "是否激活(0:禁用，1：激活)"), @Param(name = "needAudit", type = ApiParamType.ENUM, isRequired = true, rule = "0,1", desc = "是否保存执行记录(0:不保存，1:保存)"), @Param(name = "propList", type = ApiParamType.JSONARRAY, desc = "属性列表, 是否必填由定时作业组件决定"), @Param(name = "propList[0].name", type = ApiParamType.STRING, desc = "属性名"),
			@Param(name = "propList[0].value", type = ApiParamType.STRING, desc = "属性值") })
	@Output({ @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "定时作业uuid") })
	@Description(desc = "保存定时作业信息")
	@Example(example = "{name:\"测试_1\", classpath:\"codedriver.framework.scheduler.core.TestJob\", triggerType:\"simple\", repeat:\"10\", interval:\"60\", isActive:\"no\", needAudit:\"no\", beginTime:1573530069000, propList:[{name:\"p_1\",value:\"1\"},{name:\"p_2\",value:\"2\"},{name:\"p_3\",value:\"3\"},{name:\"p_4\",value:\"4\"},{name:\"p_5\",value:\"5\"}]}")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String className = jsonObj.getString("className");
		IJob jobHandler = SchedulerManager.getHandler(className);
		if (jobHandler == null) {
			throw new ScheduleHandlerNotFoundException(className);
		}
		String cron = jsonObj.getString("cron");
		if (!CronExpression.isValidExpression(cron)) {
			throw new ScheduleIllegalParameterException(cron);
		}

		JobVo jobVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<JobVo>() {
		});
		jobHandler.valid(jobVo.getPropList());
		schedulerService.saveJob(jobVo);
		String tenantUuid = TenantContext.get().getTenantUuid();
		JobObject jobObject = new JobObject.Builder(jobVo.getUuid(), jobHandler.getGroupName(), jobVo.getClassName(), tenantUuid).withCron(jobVo.getCron()).withBeginTime(jobVo.getBeginTime()).withEndTime(jobVo.getEndTime()).needAudit(jobVo.getNeedAudit()).setType("public").build();
		if (jobVo.getIsActive().intValue() == 1) {
			schedulerManager.loadJob(jobObject);
		} else {
			schedulerManager.unloadJob(jobObject);
		}

		JSONObject resultObj = new JSONObject();
		resultObj.put("uuid", jobVo.getUuid());
		return resultObj;
	}

}
