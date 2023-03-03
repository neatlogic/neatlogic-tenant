/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.scheduler;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dao.mapper.SchedulerMapper;
import neatlogic.framework.scheduler.dto.JobClassVo;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.scheduler.dto.JobPropVo;
import neatlogic.framework.scheduler.dto.JobVo;
import neatlogic.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import neatlogic.framework.scheduler.exception.ScheduleIllegalParameterException;
import neatlogic.framework.scheduler.exception.ScheduleJobNameRepeatException;
import neatlogic.framework.auth.label.SCHEDULE_JOB_MODIFY;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AuthAction(action = SCHEDULE_JOB_MODIFY.class)

@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class JobSaveApi extends PrivateApiComponentBase {
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
			@Param(name = "uuid", type = ApiParamType.STRING, isRequired = false, desc = "定时作业uuid"),
			@Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "定时作业名称"),
			@Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "定时作业组件"),
			@Param(name = "beginTime", type = ApiParamType.LONG, isRequired = false, desc = "开始时间"),
			@Param(name = "endTime", type = ApiParamType.LONG, isRequired = false, desc = "结束时间"),
			@Param(name = "cron", type = ApiParamType.STRING, isRequired = true, desc = "corn表达式"),
			@Param(name = "isActive", type = ApiParamType.ENUM, isRequired = true, rule = "0,1", desc = "是否激活(0:禁用，1：激活)"),
			@Param(name = "needAudit", type = ApiParamType.ENUM, isRequired = true, rule = "0,1", desc = "是否保存执行记录(0:不保存，1:保存)"),
			@Param(name = "propList", type = ApiParamType.JSONARRAY, desc = "属性列表, 是否必填由定时作业组件决定"),
			@Param(name = "propList[0].name", type = ApiParamType.STRING, desc = "属性名"),
			@Param(name = "propList[0].value", type = ApiParamType.STRING, desc = "属性值") })
	@Output({ @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "定时作业uuid") })
	@Description(desc = "保存定时作业信息")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String handler = jsonObj.getString("handler");
		IJob jobHandler = SchedulerManager.getHandler(handler);
		if (jobHandler == null) {
			throw new ScheduleHandlerNotFoundException(handler);
		}
		String cron = jsonObj.getString("cron");
		if (!CronExpression.isValidExpression(cron)) {
			throw new ScheduleIllegalParameterException(cron);
		}

//		JobVo jobVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<JobVo>() {});
		JobVo jobVo = JSONObject.toJavaObject(jsonObj, JobVo.class);
		jobHandler.valid(jobVo.getPropList());
		JobClassVo jobClassVo = SchedulerManager.getJobClassByClassName(handler);
		if (jobClassVo == null) {
			throw new ScheduleHandlerNotFoundException(handler);
		}
		saveJob(jobVo, jobHandler);
		String tenantUuid = TenantContext.get().getTenantUuid();
		JobObject jobObject = new JobObject.Builder(jobVo.getUuid(), jobHandler.getGroupName(), jobHandler.getClassName(), tenantUuid).withCron(jobVo.getCron()).withBeginTime(jobVo.getBeginTime()).withEndTime(jobVo.getEndTime()).needAudit(jobVo.getNeedAudit()).setType("public").build();
		if (jobVo.getIsActive().intValue() == 1) {
			schedulerManager.loadJob(jobObject);
		} else {
			schedulerManager.unloadJob(jobObject);
		}

		JSONObject resultObj = new JSONObject();
		resultObj.put("uuid", jobVo.getUuid());
		return resultObj;
	}
	
	private int saveJob(JobVo job, IJob jobHandler) throws ScheduleJobNameRepeatException {
		String uuid = job.getUuid();
		if (schedulerMapper.checkJobNameIsExists(job) > 0) {
			throw new ScheduleJobNameRepeatException(job.getName());
		}
		JobVo oldJobVo = schedulerMapper.getJobBaseInfoByUuid(uuid);
		if (oldJobVo == null) {
			schedulerMapper.insertJob(job);
		} else {
			if (oldJobVo.getIsActive().intValue() == 1) {
				JobObject jobObject = new JobObject.Builder(oldJobVo.getUuid(), jobHandler.getGroupName(), jobHandler.getClassName(), TenantContext.get().getTenantUuid()).build();
				schedulerManager.unloadJob(jobObject);
			}
			schedulerMapper.deleteJobPropByJobUuid(uuid);
			schedulerMapper.updateJob(job);
		}
		if (job.getPropList() != null && job.getPropList().size() > 0) {
			for (JobPropVo jobProp : job.getPropList()) {
				jobProp.setJobUuid(uuid);
				schedulerMapper.insertJobProp(jobProp);
			}
		}
		return 1;
	}

	public IValid name() {
		return value -> {
			JobVo vo = JSON.toJavaObject(value, JobVo.class);
			if (schedulerMapper.checkJobNameIsExists(vo) > 0) {
				return new FieldValidResultVo(new ScheduleJobNameRepeatException(vo.getName()));
			}
			return new FieldValidResultVo();
		};
	}

}
