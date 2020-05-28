package codedriver.module.tenant.api.notify;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.notify.core.INotifyTriggerHandler;
import codedriver.framework.notify.core.NotifyTriggerHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@IsActived
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
		@Param(name="moduleId", type = ApiParamType.STRING, isRequired = true, desc = "模块id")
	})
	@Output({
		@Param(explode = ValueTextVo[].class, desc = "通知触发点列表") 
	})
	@Description(desc = "通知触发点列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String moduleId = jsonObj.getString("moduleId");
		INotifyTriggerHandler handler = NotifyTriggerHandlerFactory.getHandler(moduleId);
		return handler.getNotifyTriggerList();
	}

}
