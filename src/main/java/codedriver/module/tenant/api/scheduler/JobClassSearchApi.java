package codedriver.module.tenant.api.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.ModuleUtil;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dto.JobClassVo;

@Service
@AuthAction(name = "SCHEDULE_JOB_MODIFY")
@OperationType(type = OperationTypeEnum.SEARCH)
public class JobClassSearchApi extends ApiComponentBase {

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

	@Input({ @Param(name = "currentPage", type = ApiParamType.INTEGER, isRequired = false, desc = "当前页码"), @Param(name = "pageSize", type = ApiParamType.INTEGER, isRequired = false, desc = "页大小"), @Param(name = "keyword", type = ApiParamType.STRING, isRequired = false, desc = "定时作业组件名称(支持模糊查询)"), @Param(name = "moduleId", type = ApiParamType.STRING, isRequired = false, desc = "模块id") })
	@Description(desc = "查询定时作业组件列表")
	@Output({ @Param(name = "currentPage", type = ApiParamType.INTEGER, isRequired = true, desc = "当前页码"), @Param(name = "pageSize", type = ApiParamType.INTEGER, isRequired = true, desc = "页大小"), @Param(name = "pageCount", type = ApiParamType.INTEGER, isRequired = true, desc = "总页数"), @Param(name = "rowNum", type = ApiParamType.INTEGER, isRequired = true, desc = "总行数"), @Param(name = "tbodyList", explode = JobClassVo[].class, isRequired = true, desc = "定时作业组件列表") })
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JobClassVo jobClassVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<JobClassVo>() {
		});
		List<JobClassVo> jobClassList = searchJobClassList(jobClassVo);
		JSONObject resultObj = new JSONObject();
		resultObj.put("tbodyList", jobClassList);
		resultObj.put("currentPage", jobClassVo.getCurrentPage());
		resultObj.put("pageSize", jobClassVo.getPageSize());
		resultObj.put("pageCount", jobClassVo.getPageCount());
		resultObj.put("rowNum", jobClassVo.getRowNum());
		return resultObj;
	}

	private List<JobClassVo> searchJobClassList(JobClassVo jobClassVo) {
		List<JobClassVo> jobClassList = SchedulerManager.getAllPublicJobClassList();
		List<JobClassVo> jobClassFilterList = new ArrayList<>();
		List<String> moduleList = new ArrayList<>();
		if (StringUtils.isNotBlank(jobClassVo.getModuleId())) {
			moduleList = ModuleUtil.getModuleGroup(jobClassVo.getModuleId()).getModuleIdList();
		}
		for (JobClassVo jobClass : jobClassList) {
			if (CollectionUtils.isNotEmpty(moduleList) && !moduleList.contains(jobClass.getModuleId())) {
				continue;
			}
			if (jobClassVo.getKeyword() != null && !jobClass.getName().toUpperCase().contains(jobClassVo.getKeyword().toUpperCase())) {
				continue;
			}
			jobClassFilterList.add(jobClass);
		}

		int pageSize = jobClassVo.getPageSize();
		int rowNum = jobClassFilterList.size();
		int pageCount = PageUtil.getPageCount(rowNum, pageSize);
		jobClassVo.setPageCount(pageCount);
		jobClassVo.setRowNum(rowNum);
		int startNum = jobClassVo.getStartNum();
		int endNum = startNum + pageSize;
		endNum = endNum > rowNum ? rowNum : endNum;
		List<JobClassVo> returnJobClassList = jobClassFilterList.subList(startNum, endNum);
		return returnJobClassList;
	}

}
