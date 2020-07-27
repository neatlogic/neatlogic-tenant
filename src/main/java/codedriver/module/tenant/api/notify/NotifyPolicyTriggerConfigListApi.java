package codedriver.module.tenant.api.notify;

import java.util.List;
import java.util.stream.Collectors;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyTriggerConfigListApi  extends ApiComponentBase {

	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/policy/trigger/config/list";
	}

	@Override
	public String getName() {
		return "通知策略触发动作配置列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
		@Param(name = "trigger", type = ApiParamType.STRING, isRequired = true, desc = "通知触发类型")
	})
	@Output({
		@Param(name = "notifyList", type = ApiParamType.JSONARRAY, desc = "通知触发配置列表")
	})
	@Description(desc = "通知策略触发动作配置列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
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
		JSONObject resultObj = new JSONObject();
		resultObj.put("authorityConfig",notifyPolicyHandler.getAuthorityConfig());
		resultObj.put("notifyList", new JSONArray());
		JSONObject config = notifyPolicyVo.getConfig();
		JSONArray triggerList = config.getJSONArray("triggerList");
		for(int i = 0; i < triggerList.size(); i++) {
			JSONObject triggerObj = triggerList.getJSONObject(i);
			if(trigger.equals(triggerObj.getString("trigger"))) {
				resultObj.put("notifyList", triggerObj.getJSONArray("notifyList"));
			}
		}
		return resultObj;
	}

}
