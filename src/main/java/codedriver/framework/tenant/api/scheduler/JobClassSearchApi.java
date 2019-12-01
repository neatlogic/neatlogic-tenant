package codedriver.framework.tenant.api.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Example;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.dto.JobClassVo;
import codedriver.framework.scheduler.service.SchedulerService;
@Service
@AuthAction(name="SYSTEM_JOB_EDIT")
public class JobClassSearchApi extends ApiComponentBase {

	@Autowired
	private SchedulerService schedulerService;
	
	@Override
	public String getToken() {
		return "job/class/search";
	}

	@Override
	public String getName() {
		return "查询定时作业组件列表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=false,desc="当前页码"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=false,desc="页大小"),
		@Param(name="name",type=ApiParamType.STRING,isRequired=false,desc="定时作业组件名称(支持模糊查询)"),
		@Param(name="moduleName",type=ApiParamType.STRING,isRequired=false,desc="模块名称"),
		@Param(name="type",type=ApiParamType.STRING,isRequired=false,desc="类型(flow-流程级别，task-任务级别，once-只允许配一次)")
		})
	@Description(desc="查询定时作业组件列表")
	@Example(example="{\"name\":\"自动评分job\", \"moduleName\":\"flow\",\"type\":\"flow\"}")
	@Output({
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=true,desc="当前页码"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=true,desc="页大小"),
		@Param(name="pageCount",type=ApiParamType.INTEGER,isRequired=true,desc="总页数"),
		@Param(name="rowNum",type=ApiParamType.INTEGER,isRequired=true,desc="总行数"),
		@Param(name="jobClassList",type=ApiParamType.JSONARRAY,isRequired=true,desc="定时作业组件列表"),
		@Param(name="jobClassList[0].name",type=ApiParamType.STRING,isRequired=true,desc="定时作业组件名称"),
		@Param(name="jobClassList[0].classpath",type=ApiParamType.STRING,isRequired=true,desc="定时作业组件classpath"),
		@Param(name="jobClassList[0].moduleName",type=ApiParamType.STRING,isRequired=true,desc="定时作业组件所属模块名"),
		@Param(name="jobClassList[0].moduleDesc",type=ApiParamType.STRING,isRequired=true,desc="定时作业组件所属模块描述"),
		@Param(name="jobClassList[0].type",type=ApiParamType.STRING,isRequired=true,desc="定时作业组件级别类型")
		})
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JobClassVo jobClassVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<JobClassVo>() {});
		TenantContext tenant = TenantContext.get();
		tenant.setUseDefaultDatasource(false);
		String tenantUuid = tenant.getTenantUuid();
		jobClassVo.setTenantUuid(tenantUuid);
		tenant.setUseDefaultDatasource(true);		
		List<JobClassVo> jobClassList = schedulerService.searchJobClassList(jobClassVo);		
		JSONObject resultObj = new JSONObject();
		resultObj.put("jobClassList", jobClassList);
		resultObj.put("currentPage",jobClassVo.getCurrentPage());
		resultObj.put("pageSize",jobClassVo.getPageSize());
		resultObj.put("pageCount", jobClassVo.getPageCount());
		resultObj.put("rowNum", jobClassVo.getRowNum());
		return resultObj;
	}

}
