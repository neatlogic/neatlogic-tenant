package codedriver.module.tenant.api.scheduler;

import java.util.Map;
import java.util.Map.Entry;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dto.JobClassVo;
import codedriver.framework.scheduler.exception.ScheduleHandlerNotFoundException;
@Service
@AuthAction(name="SCHEDULE_JOB_MODIFY")
@OperationType(type = OperationTypeEnum.SEARCH)
public class JobClassGetApi extends ApiComponentBase {
	
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
		Map<String, codedriver.framework.scheduler.annotation.Param> paramMap = job.initProp();
		for(Entry<String, codedriver.framework.scheduler.annotation.Param> entry : paramMap.entrySet()) {
			codedriver.framework.scheduler.annotation.Param param = entry.getValue();
			JSONObject paramObj = new JSONObject();
            paramObj.put("name", param.name());
            paramObj.put("dataType", param.dataType());
            paramObj.put("description", param.description());
            paramObj.put("required", param.required());
            inputList.add(paramObj);
		}
		JSONObject returnObj = new JSONObject();
		returnObj.put("jobClass", jobClass);
		returnObj.put("inputList", inputList);
		return returnObj;
	}

}
