package codedriver.module.tenant.api.notify;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dto.NotifyPolicyParamTypeVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@IsActived
public class NotifyPolicyParamTypeListApi extends ApiComponentBase {

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
		@Param(name = "policyHandler", type = ApiParamType.STRING, isRequired = true, desc = "通知策略类型")
	})
	@Output({
		@Param(name = "paramTypeList", explode = NotifyPolicyParamTypeVo[].class, desc = "参数类型列表") 
	})
	@Description(desc = "通知策略参数类型列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String policyHandler = jsonObj.getString("policyHandler");
		INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(policyHandler);
		if(notifyPolicyHandler == null) {
			throw new NotifyPolicyHandlerNotFoundException(policyHandler);
		}
		JSONObject resultObj = new JSONObject();
		resultObj.put("paramTypeList", notifyPolicyHandler.getParamTypeList());
		return resultObj;	
	}

}
