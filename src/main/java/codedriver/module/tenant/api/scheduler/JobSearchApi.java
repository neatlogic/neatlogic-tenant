package codedriver.module.tenant.api.scheduler;

import java.util.List;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobClassVo;
import codedriver.framework.scheduler.dto.JobVo;
import codedriver.framework.scheduler.exception.ScheduleHandlerNotFoundException;

@Service
@AuthAction(name = "SYSTEM_JOB_EDIT")
@OperationType(type = OperationTypeEnum.SEARCH)
public class JobSearchApi extends ApiComponentBase {

	@Autowired
	private SchedulerMapper schedulerMapper;

	@Override
	public String getToken() {
		return "job/search";
	}

	@Override
	public String getName() {
		return "查询定时作业列表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "currentPage", type = ApiParamType.INTEGER, isRequired = false, desc = "当前页码"), @Param(name = "pageSize", type = ApiParamType.INTEGER, isRequired = false, desc = "页大小"), @Param(name = "keyword", type = ApiParamType.STRING, isRequired = false, desc = "定时作业名称(支持模糊查询)"), @Param(name = "handler", type = ApiParamType.STRING, isRequired = false, desc = "定时作业组件className") })
	@Description(desc = "查询定时作业列表")
	@Output({ @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页码"), @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "页大小"), @Param(name = "pageCount", type = ApiParamType.INTEGER, desc = "总页数"), @Param(name = "rowNum", type = ApiParamType.INTEGER, desc = "总行数"), @Param(name = "tbodyList", explode = JobVo[].class, desc = "定时作业列表"), })
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// 判断定时作业组件是否存在
		if (jsonObj.containsKey("handler")) {
			String handler = jsonObj.getString("handler");
			JobClassVo jobClass = SchedulerManager.getJobClassByClassName(handler);
			if (jobClass == null) {
				throw new ScheduleHandlerNotFoundException(handler);
			}
		}

		JobVo jobVo = JSONObject.toJavaObject(jsonObj, JobVo.class);
		int rowNum = schedulerMapper.searchJobCount(jobVo);
		int pageCount = PageUtil.getPageCount(rowNum, jobVo.getPageSize());
		jobVo.setPageCount(pageCount);
		jobVo.setRowNum(rowNum);
		List<JobVo> jobList = schedulerMapper.searchJob(jobVo);
		JSONObject resultObj = new JSONObject();
		resultObj.put("tbodyList", jobList);
		resultObj.put("currentPage", jobVo.getCurrentPage());
		resultObj.put("pageSize", jobVo.getPageSize());
		resultObj.put("pageCount", jobVo.getPageCount());
		resultObj.put("rowNum", jobVo.getRowNum());
		return resultObj;
	}

}
