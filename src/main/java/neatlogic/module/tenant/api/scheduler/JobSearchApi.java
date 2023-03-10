/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.scheduler;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dao.mapper.SchedulerMapper;
import neatlogic.framework.scheduler.dto.JobClassVo;
import neatlogic.framework.scheduler.dto.JobVo;
import neatlogic.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import neatlogic.framework.auth.label.SCHEDULE_JOB_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AuthAction(action = SCHEDULE_JOB_MODIFY.class)

@OperationType(type = OperationTypeEnum.SEARCH)
public class JobSearchApi extends PrivateApiComponentBase {

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
