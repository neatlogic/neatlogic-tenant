package codedriver.module.tenant.api.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobAuditVo;
import codedriver.framework.scheduler.dto.JobVo;
import codedriver.framework.scheduler.exception.ScheduleJobNotFoundException;

@Service
@Transactional
@AuthAction(name = "SCHEDULE_JOB_MODIFY")
@OperationType(type = OperationTypeEnum.SEARCH)
public class JobAuditSearchApi extends ApiComponentBase {

	@Autowired
	private SchedulerMapper schedulerMapper;

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

	@Input({ @Param(name = "currentPage", type = ApiParamType.INTEGER, isRequired = false, desc = "当前页码"), @Param(name = "pageSize", type = ApiParamType.INTEGER, isRequired = false, desc = "页大小"), @Param(name = "jobUuid", type = ApiParamType.STRING, isRequired = true, desc = "定时作业uuid") })
	@Description(desc = "查询定时作业执行记录列表")
	@Output({ @Param(name = "currentPage", type = ApiParamType.INTEGER, isRequired = true, desc = "当前页码"), @Param(name = "pageSize", type = ApiParamType.INTEGER, isRequired = true, desc = "页大小"), @Param(name = "pageCount", type = ApiParamType.INTEGER, isRequired = true, desc = "总页数"), @Param(name = "rowNum", type = ApiParamType.INTEGER, isRequired = true, desc = "总行数"), @Param(name = "tbodyList", explode = JobAuditVo[].class, desc = "执行记录列表") })
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JobAuditVo jobAuditVo = JSONObject.toJavaObject(jsonObj, JobAuditVo.class);
		JobVo job = schedulerMapper.getJobByUuid(jobAuditVo.getJobUuid());
		if (job == null) {
			throw new ScheduleJobNotFoundException(jobAuditVo.getJobUuid());
		}
		int rowNum = schedulerMapper.searchJobAuditCount(jobAuditVo);
		int pageCount = PageUtil.getPageCount(rowNum, jobAuditVo.getPageSize());
		jobAuditVo.setPageCount(pageCount);
		jobAuditVo.setRowNum(rowNum);
		List<JobAuditVo> jobAuditList = schedulerMapper.searchJobAudit(jobAuditVo);

		JSONObject resultObj = new JSONObject();
		resultObj.put("tbodyList", jobAuditList);
		resultObj.put("currentPage", jobAuditVo.getCurrentPage());
		resultObj.put("pageSize", jobAuditVo.getPageSize());
		resultObj.put("pageCount", jobAuditVo.getPageCount());
		resultObj.put("rowNum", jobAuditVo.getRowNum());
		return resultObj;
	}

}
