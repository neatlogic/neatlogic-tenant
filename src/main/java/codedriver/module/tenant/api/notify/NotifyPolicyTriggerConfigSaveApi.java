package codedriver.module.tenant.api.notify;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.exception.type.ParamIrregularException;
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
public class NotifyPolicyTriggerConfigSaveApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "notify/policy/tigger/config/save";
	}

	@Override
	public String getName() {
		return "通知策略触发动作配置保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "策略uuid"),
		@Param(name = "trigger", type = ApiParamType.STRING, isRequired = true, desc = "通知触发类型"),
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "通知触发配置uuid"),
		@Param(name = "actionList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "动作列表信息"),
		@Param(name = "conditionConfig", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "条件配置信息")
	})
	@Output({})
	@Description(desc = "通知策略触发动作配置保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return null;
	}
	
	@Override
	public Object myDoTest(JSONObject jsonObj) {
		String uuid = jsonObj.getString("uuid");
		NotifyPolicyVo notifyPolicyVo = NotifyPolicyVo.notifyPolicyMap.get(uuid);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(uuid);
		}
		INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getPolicyHandler());
		if(notifyPolicyHandler == null) {
			throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getPolicyHandler());
		}
		List<ValueTextVo> notifyTriggerList = notifyPolicyHandler.getNotifyTriggerList();
		List<String> notifyTriggerValueList = notifyTriggerList.stream().map(ValueTextVo::getValue).collect(Collectors.toList());
		String trigger = jsonObj.getString("trigger");
		if(!notifyTriggerValueList.contains(trigger)) {
			throw new ParamIrregularException("参数trigger不符合格式要求");
		}
		JSONArray handlerList = jsonObj.getJSONArray("handlerList");
		JSONObject configObj = notifyPolicyVo.getConfigObj();
		JSONArray triggerList = configObj.getJSONArray("triggerList");
		for(int i = 0; i < triggerList.size(); i++) {
			JSONObject triggerObj = triggerList.getJSONObject(i);
			if(trigger.equals(triggerObj.getString("trigger"))) {
				triggerObj.put("handlerList", handlerList);
			}
		}
		notifyPolicyVo.setConfig(configObj.toJSONString());
		return null;
	}

}
