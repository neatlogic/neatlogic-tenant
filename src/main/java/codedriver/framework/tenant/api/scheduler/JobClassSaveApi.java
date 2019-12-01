package codedriver.framework.tenant.api.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.AuthAction;
import codedriver.framework.exception.core.ApiRuntimeException;
import codedriver.framework.exception.core.FrameworkExceptionMessageBase;
import codedriver.framework.exception.core.IApiExceptionMessage;
import codedriver.framework.exception.type.CustomExceptionMessage;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Example;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.dto.JobClassVo;
import codedriver.framework.scheduler.exception.SchedulerExceptionMessage;
import codedriver.framework.scheduler.service.SchedulerService;
@Service
@AuthAction(name="SYSTEM_JOB_EDIT")
public class JobClassSaveApi extends ApiComponentBase {
	
	Logger logger = LoggerFactory.getLogger(JobClassSaveApi.class);
	
	@Autowired
	private SchedulerService schedulerService;
	
	@Override
	public String getToken() {
		return "job/class/save";
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
		@Param(name="name",type=ApiParamType.STRING,isRequired=true,desc="定时作业组件名称"),
		@Param(name="classpath",type=ApiParamType.STRING,isRequired=true,desc="定时作业组件classpath"),
		@Param(name="moduleName",type=ApiParamType.STRING,isRequired=true,desc="定时作业组件所属模块名"),
		@Param(name="type",type=ApiParamType.STRING,isRequired=true,desc="定时作业组件级别类型")
		})
	@Description(desc="保存定时作业组件信息")
	@Example(example="{name:\"测试_1\", classpath:\"codedriver.framework.scheduler.core.TestJob\", moduleName:\"framework\", type:\"task\"}")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JobClassVo jobClassVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<JobClassVo>() {});
		TenantContext tenant = TenantContext.get();
		tenant.setUseDefaultDatasource(false);
		String tenantUuid = tenant.getTenantUuid();
		jobClassVo.setTenantUuid(tenantUuid);
		tenant.setUseDefaultDatasource(true);
		schedulerService.saveJobClass(jobClassVo);
		return "OK";
	}

}
