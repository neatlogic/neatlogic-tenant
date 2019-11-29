package codedriver.framework.tenant.api.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.api.core.ApiParamType;
import codedriver.framework.common.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Example;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.dto.JobAuditVo;
import codedriver.framework.scheduler.service.SchedulerService;
@Service
@Transactional
@AuthAction(name="SYSTEM_JOB_EDIT")
public class JobAuditSearchApi extends ApiComponentBase {

	@Autowired
	private SchedulerService schedulerService;
	
	@Override
	public String getToken() {
		return "job/audit/search";
	}

	@Override
	public String getName() {
		return "查询定时作业执行记录列表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=false,desc="当前页码"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=false,desc="页大小"),
		@Param(name="jobId",type=ApiParamType.LONG,isRequired=true,desc="定时作业id")
		})
	@Description(desc="查询定时作业执行记录列表")
	@Example(example="{\"jobId\":1}")
	@Output({
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=true,desc="当前页码"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=true,desc="页大小"),
		@Param(name="pageCount",type=ApiParamType.INTEGER,isRequired=true,desc="总页数"),
		@Param(name="rowNum",type=ApiParamType.INTEGER,isRequired=true,desc="总行数"),
		@Param(name="jobAuditList",type=ApiParamType.JSONARRAY,isRequired=true,desc="执行记录列表"),
		@Param(name="jobAuditList[0].id",type=ApiParamType.LONG,isRequired=true,desc="记录id"),
		@Param(name="jobAuditList[0].jobUuid",type=ApiParamType.STRING,isRequired=true,desc="定时作业uuid"),
		@Param(name="jobAuditList[0].startTime",type=ApiParamType.LONG,isRequired=true,desc="开始时间"),
		@Param(name="jobAuditList[0].endTime",type=ApiParamType.LONG,isRequired=true,desc="结束时间"),
		@Param(name="jobAuditList[0].state",type=ApiParamType.STRING,isRequired=true,desc="执行状态(success:成功；error异常；processing:进行中)"),
		@Param(name="jobAuditList[0].isLogEmpty",type=ApiParamType.INTEGER,isRequired=true,desc="日志是否为空")
		})
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JobAuditVo jobAuditVo = new JobAuditVo();
		String jobUuid = jsonObj.getString("jobUuid");
		jobAuditVo.setJobUuid(jobUuid);
		if(jsonObj.containsKey("currentPage")) {
			Integer currentPage = jsonObj.getInteger("currentPage");
			jobAuditVo.setCurrentPage(currentPage);
		}
		if(jsonObj.containsKey("pageSize")) {
			Integer pageSize = jsonObj.getInteger("pageSize");
			jobAuditVo.setPageSize(pageSize);
		}		
		List<JobAuditVo> jobAuditList = schedulerService.searchJobAuditList(jobAuditVo);
		JSONObject resultObj = new JSONObject();
		resultObj.put("jobAuditList", jobAuditList);
		resultObj.put("currentPage",jobAuditVo.getCurrentPage());
		resultObj.put("pageSize",jobAuditVo.getPageSize());
		resultObj.put("pageCount", jobAuditVo.getPageCount());
		resultObj.put("rowNum", jobAuditVo.getRowNum());
		return resultObj;
	}

}
