/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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
