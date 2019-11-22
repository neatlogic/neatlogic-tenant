package codedriver.framework.tenant.api.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Example;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobClassVo;
import codedriver.framework.tenant.service.SchedulerService;
@Service
public class SavaJobClassApi extends ApiComponentBase {
	
	Logger logger = LoggerFactory.getLogger(GetJobByIdApi.class);
	
	@Autowired
	private SchedulerService schedulerService;
	
	@Override
	public String getToken() {
		return "savaJobClassApi";
	}

	@Override
	public String getName() {
		return "保存定时作业组件信息";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name="name",type="String",isRequired="true",desc="定时作业组件名称"),
		@Param(name="classpath",type="String",isRequired="true",desc="定时作业组件classpath"),
		@Param(name="moduleName",type="String",isRequired="true",desc="定时作业组件所属模块名"),
		@Param(name="type",type="String",isRequired="true",desc="定时作业组件级别类型")
		})
	@Description(desc="保存定时作业组件信息")
	@Example(example="{name:\"测试_1\", classpath:\"codedriver.framework.scheduler.core.TestJob\", moduleName:\"framework\", type:\"task\"}")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String type = jsonObj.getString("type");	
		String name = jsonObj.getString("name");
		String classpath = jsonObj.getString("classpath");	
		String moduleName = jsonObj.getString("moduleName");
		JobClassVo jobClassVo = new JobClassVo();
		jobClassVo.setName(name);
		jobClassVo.setType(type);
		jobClassVo.setClasspath(classpath);
		jobClassVo.setModuleName(moduleName);
		TenantContext tenant = TenantContext.get();
		tenant.setUseDefaultDatasource(true);
		schedulerService.saveJobClass(jobClassVo);
		return "OK";
	}

}
