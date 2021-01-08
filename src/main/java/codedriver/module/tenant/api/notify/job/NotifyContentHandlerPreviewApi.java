package codedriver.module.tenant.api.notify.job;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.notify.core.INotifyContentHandler;
import codedriver.framework.notify.core.NotifyContentHandlerFactory;
import codedriver.framework.notify.exception.NotifyContentHandlerNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyContentHandlerPreviewApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "notify/content/handler/preview";
	}

	@Override
	public String getName() {
		return "获取通知内容插件预览视图";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "handler", type = ApiParamType.STRING, isRequired = true,desc = "通知内容插件")})
	@Output({@Param(name = "content",type = ApiParamType.STRING ,desc = "预览视图HTML")})
	@Description(desc = "获取通知内容插件预览视图")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String handler = jsonObj.getString("handler");
		INotifyContentHandler notifyContentHandler = NotifyContentHandlerFactory.getHandler(handler);
		if(notifyContentHandler == null){
			throw new NotifyContentHandlerNotFoundException(handler);
		}
		JSONObject result = new JSONObject();
		result.put("content",notifyContentHandler.preview());
		return result;
	}
}
