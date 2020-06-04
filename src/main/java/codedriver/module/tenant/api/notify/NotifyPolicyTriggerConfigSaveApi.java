package codedriver.module.tenant.api.notify;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.util.SnowflakeUtil;
@Service
@Transactional
@IsActived
public class NotifyPolicyTriggerConfigSaveApi extends ApiComponentBase {

	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/policy/trigger/config/save";
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
		@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
		@Param(name = "trigger", type = ApiParamType.STRING, isRequired = true, desc = "通知触发类型"),
		@Param(name = "id", type = ApiParamType.LONG, desc = "通知触发配置id"),
		@Param(name = "actionList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "动作列表信息"),
		@Param(name = "conditionConfig", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "条件配置信息")
	})
	@Output({
		@Param(name = "id", type = ApiParamType.LONG, desc = "通知触发配置id")
	})
	@Description(desc = "通知策略触发动作配置保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		Long policyId = jsonObj.getLong("policyId");
		NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(policyId.toString());
		}
		INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
		if(notifyPolicyHandler == null) {
			throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
		}
		List<ValueTextVo> notifyTriggerList = notifyPolicyHandler.getNotifyTriggerList();
		List<String> notifyTriggerValueList = notifyTriggerList.stream().map(ValueTextVo::getValue).collect(Collectors.toList());
		String trigger = jsonObj.getString("trigger");
		if(!notifyTriggerValueList.contains(trigger)) {
			throw new ParamIrregularException("参数trigger不符合格式要求");
		}
		Long id = jsonObj.getLong("id");
		JSONArray actionList = jsonObj.getJSONArray("actionList");
		JSONObject conditionConfig = jsonObj.getJSONObject("conditionConfig");
		JSONObject config = notifyPolicyVo.getConfig();
		JSONArray triggerList = config.getJSONArray("triggerList");
		for(int i = 0; i < triggerList.size(); i++) {
			JSONObject triggerObj = triggerList.getJSONObject(i);
			if(trigger.equals(triggerObj.getString("trigger"))) {
				JSONArray notifyList = triggerObj.getJSONArray("notifyList");
				if(id != null) {
					boolean isExists = false;
					for(int j = 0; j < notifyList.size(); j++) {
						JSONObject notifyObj = notifyList.getJSONObject(j);
						if(id.equals(notifyObj.getLong("id"))) {
							notifyObj.put("actionList", actionList);
							notifyObj.put("conditionConfig", conditionConfig);
							isExists = true;
						}
					}
					if(!isExists) {
						//TODO 抛异常
					}
					resultObj.put("id", id);
				}else {
					JSONObject notifyObj = new JSONObject();
					id = SnowflakeUtil.uniqueLong();
					notifyObj.put("id", id);
					notifyObj.put("actionList", actionList);
					notifyObj.put("conditionConfig", conditionConfig);
					notifyList.add(notifyObj);
					resultObj.put("id", id);
				}
				triggerObj.put("notifyList", notifyList);
			}
		}
		notifyPolicyVo.setConfig(config.toJSONString());
		notifyMapper.updateNotifyPolicyById(notifyPolicyVo);
		return resultObj;
	}

}
