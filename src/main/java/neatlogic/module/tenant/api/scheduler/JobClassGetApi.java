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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.SCHEDULE_JOB_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dto.JobClassVo;
import neatlogic.framework.scheduler.dto.JobPropVo;
import neatlogic.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
@Service
@AuthAction(action = SCHEDULE_JOB_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class JobClassGetApi extends PrivateApiComponentBase {
	
	@Override
	public String getToken() {
		return "job/class/get";
	}

	@Override
	public String getName() {
		return "nmtas.jobclassgetapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
			@Param(name = "className", type = ApiParamType.STRING, isRequired = true, desc = "common.classname")
	})
	@Description(desc="nmtas.jobclassgetapi.getname")
	@Output({
			@Param(name = "jobClass", explode = JobClassVo.class, desc="common.schedulejobclassinfo"),
			@Param(name = "propList", explode = JobPropVo[].class, desc = "common.attributelist")
		})
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String className = jsonObj.getString("className");
		//判断定时作业组件是否存在
		JobClassVo jobClass = SchedulerManager.getJobClassByClassName(className);
		if(jobClass == null) {
			throw new ScheduleHandlerNotFoundException(className);
		}
		List<JobPropVo> propList = new ArrayList<>();
		IJob job = SchedulerManager.getHandler(jobClass.getClassName());
		Map<String, neatlogic.framework.scheduler.annotation.Param> paramMap = job.initProp();
		for (Map.Entry<String, neatlogic.framework.scheduler.annotation.Param> entry : paramMap.entrySet()) {
			neatlogic.framework.scheduler.annotation.Param param = entry.getValue();
			JobPropVo jobPropVo = new JobPropVo();
			jobPropVo.setName(param.name());
			jobPropVo.setDataType(param.dataType());
			jobPropVo.setDescription(param.description());
			jobPropVo.setRequired(param.required());
			jobPropVo.setSort(param.sort());
			jobPropVo.setHelp(param.help());
			propList.add(jobPropVo);
		}
		//排序
		propList.sort(Comparator.comparing(JobPropVo::getSort, Comparator.nullsFirst(Comparator.naturalOrder())));
		JSONObject returnObj = new JSONObject();
		returnObj.put("jobClass", jobClass);
		returnObj.put("propList", propList);
		return returnObj;
	}

}
