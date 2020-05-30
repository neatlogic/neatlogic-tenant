package codedriver.module.tenant.api.notify;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
public class NotifyPolicySaveApi  extends ApiComponentBase {

	@Override
	public String getToken() {
		return "notify/policy/save";
	}

	@Override
	public String getName() {
		return "通知策略信息保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "策略uuid"),
		@Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "策略名"),
		@Param(name = "policyHandler", type = ApiParamType.STRING, isRequired = true, desc = "通知策略类型")
	})
	@Output({
		@Param(name = "notifyPolicy", explode = NotifyPolicyVo.class, desc = "策略信息")
	})
	@Description(desc = "通知策略信息保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return null;
	}
	
	@Override
	public Object myDoTest(JSONObject jsonObj) {
		String policyHandler = jsonObj.getString("policyHandler");
		INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(policyHandler);
		if(notifyPolicyHandler == null) {
			throw new NotifyPolicyHandlerNotFoundException(policyHandler);
		}
		String uuid = jsonObj.getString("uuid");
		String name = jsonObj.getString("name");
		if(StringUtils.isNotBlank(uuid)) {
			NotifyPolicyVo notifyPolicyVo = NotifyPolicyVo.notifyPolicyMap.get(uuid);
			if(notifyPolicyVo == null) {
				throw new NotifyPolicyNotFoundException(uuid);
			}
			notifyPolicyVo.setName(name);
			notifyPolicyVo.setLcd(new Date());
			notifyPolicyVo.setLcu(UserContext.get().getUserUuid(true));
			notifyPolicyVo.setLcuName(UserContext.get().getUserName());
			return notifyPolicyVo;
		}else {
			
			NotifyPolicyVo notifyPolicyVo = new NotifyPolicyVo(name, policyHandler);
//			notifyPolicyVo.setPolicyHandler(policyHandler);
//			notifyPolicyVo.setName(name);
			notifyPolicyVo.setFcd(new Date());
			notifyPolicyVo.setFcu(UserContext.get().getUserUuid(true));
			notifyPolicyVo.setFcuName(UserContext.get().getUserName());
			JSONObject configObj = new JSONObject();
			JSONArray triggerList = new JSONArray();
			for (ValueTextVo notifyTrigger : notifyPolicyHandler.getNotifyTriggerList()) {
				JSONObject triggerObj = new JSONObject();
				triggerObj.put("trigger", notifyTrigger.getValue());
				triggerObj.put("triggerName", notifyTrigger.getText());
				triggerObj.put("handlerList", new JSONArray());
				triggerList.add(triggerObj);
			}
			configObj.put("triggerList", triggerList);
			configObj.put("paramList", new JSONArray());
			configObj.put("templateList", new JSONArray());
			notifyPolicyVo.setConfig(configObj.toJSONString());
			NotifyPolicyVo.notifyPolicyMap.put(notifyPolicyVo.getUuid(), notifyPolicyVo);
			return notifyPolicyVo;
		}
	}

}
