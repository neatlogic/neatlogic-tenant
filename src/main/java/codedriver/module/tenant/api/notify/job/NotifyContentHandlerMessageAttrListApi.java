package codedriver.module.tenant.api.notify.job;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.notify.core.INotifyContentHandler;
import codedriver.framework.notify.core.NotifyContentHandlerFactory;
import codedriver.framework.notify.core.NotifyHandlerFactory;
import codedriver.framework.notify.exception.NotifyContentHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyHandlerNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyContentHandlerMessageAttrListApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "notify/content/handler/messageattr/list";
	}

	@Override
	public String getName() {
		return "获取通知消息属性列表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "handler", type = ApiParamType.STRING,isRequired = true, desc = "通知内容插件"),
			@Param(name = "notifyHandler", type = ApiParamType.STRING, isRequired = true,desc = "通知方式插件")
	})
	@Output({})
	@Description(desc = "获取通知消息属性列表")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String handler = jsonObj.getString("handler");
		String notifyHandler = jsonObj.getString("notifyHandler");
		INotifyContentHandler notifyContentHandler = NotifyContentHandlerFactory.getHandler(handler);
		if(notifyContentHandler == null){
			throw new NotifyContentHandlerNotFoundException(handler);
		}
		if(NotifyHandlerFactory.getHandler(notifyHandler) == null){
			throw new NotifyHandlerNotFoundException(notifyHandler);
		}
		return notifyContentHandler.getMessageAttrList(notifyHandler);
	}
}
