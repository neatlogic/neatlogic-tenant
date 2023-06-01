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

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dto.JobClassVo;
import neatlogic.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import neatlogic.framework.auth.label.SCHEDULE_JOB_MODIFY;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
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
		return "获取定时作业组件信息";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({@Param(name="className",type=ApiParamType.STRING,isRequired=true,desc="定时作业组件className")})
	@Description(desc="获取定时作业组件信息")
	@Output({
		@Param(name="Return",explode=JobClassVo.class,isRequired=true,desc="定时作业组件信息"),
		@Param(name="inputList",type=ApiParamType.JSONARRAY,isRequired=true,desc="属性列表"),
		@Param(name="inputList[0].name",type=ApiParamType.STRING,isRequired=true,desc="属性名称"),
		@Param(name="inputList[0].dataType",type=ApiParamType.STRING,isRequired=true,desc="属性数据类型"),
		@Param(name="inputList[0].description",type=ApiParamType.STRING,isRequired=true,desc="属性说明"),
		@Param(name="inputList[0].required",type=ApiParamType.BOOLEAN,isRequired=true,desc="属性是否必填")		
		})
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String className = jsonObj.getString("className");
		//判断定时作业组件是否存在
		JobClassVo jobClass = SchedulerManager.getJobClassByClassName(className);
		if(jobClass == null) {
			throw new ScheduleHandlerNotFoundException(className);
		}
		JSONArray inputList = new JSONArray();
		IJob job = SchedulerManager.getHandler(jobClass.getClassName());
		Map<String, neatlogic.framework.scheduler.annotation.Param> paramMap = job.initProp();
		for(Entry<String, neatlogic.framework.scheduler.annotation.Param> entry : paramMap.entrySet()) {
			neatlogic.framework.scheduler.annotation.Param param = entry.getValue();
			JSONObject paramObj = new JSONObject();
            paramObj.put("name", param.name());
            paramObj.put("dataType", param.dataType());
            paramObj.put("description", param.description());
            paramObj.put("required", param.required());
			paramObj.put("sort", param.sort());
            inputList.add(paramObj);
		}
		//排序
		inputList.sort(Comparator.comparing(obj->((JSONObject)obj)
				.getIntValue("sort"),Comparator.nullsFirst(Comparator.naturalOrder()))
		);
		JSONObject returnObj = new JSONObject();
		returnObj.put("jobClass", jobClass);
		returnObj.put("inputList", inputList);
		return returnObj;
	}

}
