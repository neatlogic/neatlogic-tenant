package codedriver.framework.tenant.api.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

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
		@Param(name="currentPage",type="Integer",isRequired="false",desc="当前页码"),
		@Param(name="pageSize",type="Integer",isRequired="false",desc="页大小"),
		@Param(name="name",type="String",isRequired="false",desc="定时作业组件名称(支持模糊查询)"),
		@Param(name="moduleName",type="String",isRequired="false",desc="模块名称"),
		@Param(name="type",type="String",isRequired="false",desc="类型(flow-流程级别，task-任务级别，once-只允许配一次)")
		})
	@Description(desc="查询定时作业组件列表")
	@Example(example="{\"name\":\"自动评分job\", \"moduleName\":\"flow\",\"type\":\"flow\"}")
	@Output({
		@Param(name="currentPage",type="Integer",isRequired="true",desc="当前页码"),
		@Param(name="pageSize",type="Integer",isRequired="true",desc="页大小"),
		@Param(name="pageCount",type="Integer",isRequired="true",desc="总页数"),
		@Param(name="rowNum",type="Integer",isRequired="true",desc="总行数"),
		@Param(name="jobClassList",type="Array",isRequired="true",desc="定时作业组件列表"),
		@Param(name="name",type="String",isRequired="true",desc="定时作业组件名称"),
		@Param(name="classpath",type="String",isRequired="true",desc="定时作业组件classpath"),
		@Param(name="moduleName",type="String",isRequired="true",desc="定时作业组件所属模块名"),
		@Param(name="moduleDesc",type="String",isRequired="true",desc="定时作业组件所属模块描述"),
		@Param(name="type",type="String",isRequired="true",desc="定时作业组件级别类型")
		})
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JobClassVo jobClassVo = new JobClassVo();
		if(jsonObj.containsKey("name")) {
			String name = jsonObj.getString("name");
			jobClassVo.setName(name);
		}
		if(jsonObj.containsKey("moduleName")) {
			String moduleName = jsonObj.getString("moduleName");
			jobClassVo.setModuleName(moduleName);
		}
		if(jsonObj.containsKey("name")) {
			String type = jsonObj.getString("type");
			jobClassVo.setType(type);
		}
		if(jsonObj.containsKey("currentPage")) {
			Integer currentPage = jsonObj.getInteger("currentPage");
			jobClassVo.setCurrentPage(currentPage);
		}
		if(jsonObj.containsKey("pageSize")) {
			Integer pageSize = jsonObj.getInteger("pageSize");
			jobClassVo.setPageSize(pageSize);
		}
		TenantContext.get().setUseDefaultDatasource(true);
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
