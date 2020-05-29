package codedriver.module.tenant.api.notify;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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
		@Param(name = "policyUuid", type = ApiParamType.STRING, isRequired = true, desc = "策略uuid"),
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
		JSONObject resultObj = new JSONObject();
		String policyUuid = jsonObj.getString("policyUuid");
		NotifyPolicyVo notifyPolicyVo = NotifyPolicyVo.notifyPolicyMap.get(policyUuid);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(policyUuid);
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
		String uuid = jsonObj.getString("uuid");
		JSONArray actionList = jsonObj.getJSONArray("actionList");
		JSONObject conditionConfig = jsonObj.getJSONObject("conditionConfig");
		JSONObject configObj = notifyPolicyVo.getConfigObj();
		JSONArray triggerList = configObj.getJSONArray("triggerList");
		for(int i = 0; i < triggerList.size(); i++) {
			JSONObject triggerObj = triggerList.getJSONObject(i);
			if(trigger.equals(triggerObj.getString("trigger"))) {
				JSONArray notifyList = triggerObj.getJSONArray("notifyList");
				if(StringUtils.isNotBlank(uuid)) {
					boolean isExists = false;
					for(int j = 0; j < notifyList.size(); j++) {
						JSONObject notifyObj = notifyList.getJSONObject(j);
						if(uuid.equals(notifyObj.getString("uuid"))) {
							notifyObj.put("actionList", actionList);
							notifyObj.put("conditionConfig", conditionConfig);
							isExists = true;
						}
					}
					if(!isExists) {
						//TODO 抛异常
					}
				}else {
					JSONObject notifyObj = new JSONObject();
					notifyObj.put("uuid", UUID.randomUUID().toString().replace("-", ""));
					notifyObj.put("actionList", actionList);
					notifyObj.put("conditionConfig", conditionConfig);
					notifyList.add(notifyObj);
				}
				triggerObj.put("notifyList", notifyList);
				resultObj.put("notifyList", notifyList);
			}
		}
		notifyPolicyVo.setConfig(configObj.toJSONString());
		return resultObj;
	}

}
