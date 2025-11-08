/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.scheduler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.SCHEDULE_JOB_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.dao.mapper.SchedulerMapper;
import neatlogic.framework.scheduler.dto.JobAuditVo;
import neatlogic.framework.scheduler.exception.ScheduleJobAuditNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = SCHEDULE_JOB_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class JobAuditLogGetApi extends PrivateApiComponentBase {

	@Autowired
	private SchedulerMapper schedulerMapper;

	@Override
	public String getToken() {
		return "job/audit/log/get";
	}

	@Override
	public String getName() {
		return "获取定时作业执行记录日志";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "auditId", type = ApiParamType.LONG, isRequired = true, desc = "定时作业执行记录id") })
	@Output({ @Param(name = "Return", type = ApiParamType.STRING, isRequired = true, desc = "日志内容") })
	@Description(desc = "获取定时作业执行记录日志")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long auditId = jsonObj.getLong("auditId");
		JobAuditVo jobAudit = schedulerMapper.getJobAuditById(auditId);
		if (jobAudit == null) {
			throw new ScheduleJobAuditNotFoundException(auditId);
		}
		return jobAudit.getContent();
	}

}
