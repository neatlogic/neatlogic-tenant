package codedriver.framework.tenant.api.scheduler;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.exception.ApiRuntimeException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Example;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.JobBase;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobClassVo;
import codedriver.framework.scheduler.exception.SchedulerExceptionMessage;
@Service
public class GetJobClassByClasspathApi extends ApiComponentBase {
	
	Logger logger = LoggerFactory.getLogger(GetJobClassByClasspathApi.class);
	
	@Autowired
	private SchedulerMapper schedulerMapper;
	
	@Override
	public String getToken() {
		return "getJobClassByClasspathApi";
	}

	@Override
	public String getName() {
		return "获取定时作业组件信息";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({@Param(name="classpath",type="String",isRequired="true",desc="定时作业组件classpath")})
	@Description(desc="获取定时作业组件信息")
	@Example(example="{\"classpath\":\"codedriver.framework.scheduler.core.TestJob\"}")
	@Output({
		@Param(name="name",type="String",isRequired="true",desc="定时作业组件名称"),
		@Param(name="classpath",type="String",isRequired="true",desc="定时作业组件classpath"),
		@Param(name="moduleName",type="String",isRequired="true",desc="定时作业组件所属模块名"),
		@Param(name="type",type="String",isRequired="true",desc="定时作业组件级别类型"),
		@Param(name="inputList",type="Array",isRequired="true",desc="参数列表"),
		@Param(name="inputList[0].name",type="String",isRequired="true",desc="参数名称"),
		@Param(name="inputList[0].dataType",type="String",isRequired="true",desc="参数数据类型"),
		@Param(name="inputList[0].description",type="String",isRequired="true",desc="参数说明"),
		@Param(name="inputList[0].required",type="Boolean",isRequired="true",desc="参数是否必填")		
		})
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String classpath = jsonObj.getString("classpath");
		JobClassVo jobClassVo = new JobClassVo();
		jobClassVo.setClasspath(classpath);
		TenantContext tenant = TenantContext.get();
		tenant.setUseDefaultDatasource(true);
		JobClassVo jobClass = schedulerMapper.getJobClassByClasspath(jobClassVo);
		if(jobClass == null) {
			SchedulerExceptionMessage message = new SchedulerExceptionMessage("定时作业组件："+ classpath + " 不存在");
			logger.error(message.toString());
			throw new ApiRuntimeException(message);
		}
		JSONArray inputList = new JSONArray();
		IJob job = SchedulerManager.getInstance(jobClass.getClasspath());
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
