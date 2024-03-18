/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.scheduler;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.dao.mapper.SchedulerMapper;
import neatlogic.framework.scheduler.dto.JobAuditVo;
import neatlogic.framework.scheduler.exception.ScheduleJobAuditNotFoundException;
import neatlogic.framework.auth.label.SCHEDULE_JOB_MODIFY;
import com.alibaba.fastjson.JSONObject;
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
