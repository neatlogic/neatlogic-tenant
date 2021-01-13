package codedriver.module.tenant.api.notify;

import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyParamTypeListApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "notify/policy/paramtype/list";
	}

	@Override
	public String getName() {
		return "通知策略参数类型列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "通知策略类型")
	})
	@Output({
		@Param(explode = ValueTextVo[].class, desc = "参数类型列表") 
	})
	@Description(desc = "通知策略参数类型列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String handler = jsonObj.getString("handler");
		INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(handler);
		if(notifyPolicyHandler == null) {
			throw new NotifyPolicyHandlerNotFoundException(handler);
		}
		return notifyPolicyHandler.getParamTypeList();	
	}

}
