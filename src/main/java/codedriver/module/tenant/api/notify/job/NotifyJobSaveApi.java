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
import org.springframework.transaction.annotation.Transactional;

@Service
@AuthAction(name = "NOTIFY_JOB_MODIFY")
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class NotifyJobSaveApi extends PrivateApiComponentBase {


	@Autowired
	private SchedulerManager schedulerManager;

	@Override
	public String getToken() {
		return "notify/job/save";
	}

	@Override
	public String getName() {
		return "保存通知定时任务";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "id", type = ApiParamType.LONG, desc = "定时任务ID"),
			@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired = true, xss = true, desc = "名称"),
			@Param(name = "handler", type = ApiParamType.STRING, isRequired = true,desc = "插件"),
			@Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "插件自带参数"),
			@Param(name = "notifyHandler", type = ApiParamType.STRING, isRequired = true, desc = "通知方式插件"),
			@Param(name = "title", type = ApiParamType.STRING, isRequired = true,desc = "通知消息标题"),
			@Param(name = "content", type = ApiParamType.STRING, isRequired = true,desc = "通知消息内容"),
			@Param(name = "toList", type = ApiParamType.JSONARRAY,isRequired = true, desc = "收件人列表，格式[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\",\"processUserType#major\",\"custom#123@qq.com\"]"),
			@Param(name = "ccList", type = ApiParamType.JSONARRAY,desc = "抄送人列表，格式同收件人"),
			@Param(name = "cron", type = ApiParamType.STRING, isRequired = true, desc = "corn表达式"),
			@Param(name = "isActive", type = ApiParamType.ENUM, isRequired = true, rule = "0,1", desc = "是否激活(0:禁用，1：激活)"),
	})
	@Output({ @Param(name = "id", type = ApiParamType.LONG, desc = "定时任务ID") })
	@Description(desc = "保存通知定时任务")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return null;
	}



}
