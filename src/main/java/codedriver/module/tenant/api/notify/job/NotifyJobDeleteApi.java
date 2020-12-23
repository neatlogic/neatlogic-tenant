package codedriver.module.tenant.api.notify.job;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.notify.dao.mapper.NotifyJobMapper;
import codedriver.framework.notify.exception.NotifyJobNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.module.tenant.auth.label.NOTIFY_JOB_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AuthAction(action = NOTIFY_JOB_MODIFY.class)
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class NotifyJobDeleteApi extends PrivateApiComponentBase {

	@Autowired
	private NotifyJobMapper notifyJobMapper;

	@Autowired
	private SchedulerMapper schedulerMapper;

	@Autowired
	private SchedulerManager schedulerManager;

	@Override
	public String getToken() {
		return "notify/job/delete";
	}

	@Override
	public String getName() {
		return "删除通知定时任务";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true,desc = "定时任务ID")})
	@Output({})
	@Description(desc = "删除通知定时任务")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long id = jsonObj.getLong("id");
		if(notifyJobMapper.getJobBaseInfoById(id) == null){
			throw new NotifyJobNotFoundException(id);
		}
		notifyJobMapper.deleteJobById(id);
		notifyJobMapper.deleteReceiverByJobId(id);

		// TODO 清除定时任务
		return null;
	}
}
