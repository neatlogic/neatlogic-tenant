package codedriver.module.tenant.api.notify.job;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.scheduler.core.SchedulerManager;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AuthAction(name = "NOTIFY_JOB_MODIFY")
@OperationType(type = OperationTypeEnum.UPDATE)
public class NotifyJobStatusUpdateApi extends PrivateApiComponentBase {

	@Autowired
	private SchedulerManager schedulerManager;

	@Override
	public String getToken() {
		return "notify/job/status/update";
	}

	@Override
	public String getName() {
		return "修改通知定时任务激活状态";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "定时任务ID"),
			@Param(name = "isActive", type = ApiParamType.ENUM, isRequired = true, rule = "0,1", desc = "是否激活(0:禁用，1：激活)"),
	})
	@Output({})
	@Description(desc = "修改通知定时任务激活状态")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {

		return null;
	}
}
