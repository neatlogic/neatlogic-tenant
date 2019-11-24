package codedriver.framework.tenant.api.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

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
public class SearchJobAuditListApi extends ApiComponentBase {

	@Autowired
	private SchedulerService schedulerService;
	
	@Override
	public String getToken() {
		return "searchJobAuditListApi";
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
		@Param(name="currentPage",type="Integer",isRequired="false",desc="当前页码"),
		@Param(name="pageSize",type="Integer",isRequired="false",desc="页码大小"),
		@Param(name="jobId",type="Long",isRequired="true",desc="定时作业id")
		})
	@Description(desc="查询定时作业执行记录列表")
	@Example(example="{\"jobId\":1}")
	@Output({
		@Param(name="currentPage",type="Integer",isRequired="true",desc="当前页码"),
		@Param(name="pageSize",type="Integer",isRequired="true",desc="页码大小"),
		@Param(name="pageCount",type="Integer",isRequired="true",desc="总页数"),
		@Param(name="rowNum",type="Integer",isRequired="true",desc="总行数"),
		@Param(name="jobAuditList",type="Array",isRequired="true",desc="执行记录列表"),
		@Param(name="jobAuditList[0].id",type="Long",isRequired="true",desc="记录id"),
		@Param(name="jobAuditList[0].jobId",type="Long",isRequired="true",desc="定时作业id"),
		@Param(name="jobAuditList[0].startTime",type="Long",isRequired="true",desc="开始时间"),
		@Param(name="jobAuditList[0].endTime",type="Long",isRequired="true",desc="结束时间"),
		@Param(name="jobAuditList[0].state",type="String",isRequired="true",desc="执行状态"),
		@Param(name="jobAuditList[0].isLogEmpty",type="Integer",isRequired="true",desc="日志是否为空")
		})
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JobAuditVo jobAuditVo = new JobAuditVo();
		Long jobId = jsonObj.getLong("jobId");
		jobAuditVo.setJobId(jobId);
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
		resultObj.put("pageCount", jobAuditVo.getPageCount());
		resultObj.put("rowNum", jobAuditVo.getRowNum());
		return resultObj;
	}

}
