package codedriver.module.tenant.api.notify;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyTriggerListApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "notify/trigger/list";
	}

	@Override
	public String getName() {
		return "通知触发点列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "通知策略类型")
	})
	@Output({
		@Param(explode = ValueTextVo[].class, desc = "通知触发点列表") 
	})
	@Description(desc = "通知触发点列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String handler = jsonObj.getString("handler");
		INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(handler);
		if(notifyPolicyHandler == null) {
			throw new NotifyPolicyHandlerNotFoundException(handler);
		}
		return notifyPolicyHandler.getNotifyTriggerList();
	}

}
