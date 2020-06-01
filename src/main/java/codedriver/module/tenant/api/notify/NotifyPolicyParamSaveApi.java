package codedriver.module.tenant.api.notify;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.notify.dto.NotifyPolicyParamVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
public class NotifyPolicyParamSaveApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "notify/policy/param/save";
	}

	@Override
	public String getName() {
		return "通知策略参数保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "policyUuid", type = ApiParamType.STRING, isRequired = true, desc = "策略uuid"),
		@Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "参数名"),
		@Param(name = "type", type = ApiParamType.STRING, isRequired = true, desc = "参数类型"),
		@Param(name = "description", type = ApiParamType.STRING, isRequired = true, desc = "参数描述"),
		@Param(name = "config", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "配置信息"),
	})
	@Output({
		@Param(name = "paramList", explode = NotifyPolicyParamVo[].class, desc = "参数列表")
	})
	@Description(desc = "通知策略参数保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Object myDoTest(JSONObject jsonObj) {
		String policyUuid = jsonObj.getString("policyUuid");
		NotifyPolicyVo notifyPolicyVo = NotifyPolicyVo.notifyPolicyMap.get(policyUuid);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(policyUuid);
		}
		String name = jsonObj.getString("name");
		String type = jsonObj.getString("type");
		String description = jsonObj.getString("description");
		String config = jsonObj.getString("config");
		boolean isNew = true;
		JSONObject configObj = notifyPolicyVo.getConfigObj();
		List<NotifyPolicyParamVo> paramList = JSON.parseArray(configObj.getJSONArray("paramList").toJSONString(), NotifyPolicyParamVo.class);
		for(NotifyPolicyParamVo notifyPolicyParamVo : paramList) {
			if(name.equals(notifyPolicyParamVo.getName())) {
				notifyPolicyParamVo.setType(type);
				notifyPolicyParamVo.setDescription(description);
				notifyPolicyParamVo.setConfig(config);
				isNew = false;
			}
		}
		if(isNew) {
			NotifyPolicyParamVo notifyPolicyParamVo = new NotifyPolicyParamVo();
			notifyPolicyParamVo.setName(name);
			notifyPolicyParamVo.setType(type);
			notifyPolicyParamVo.setDescription(description);
			notifyPolicyParamVo.setConfig(config);
			paramList.add(0, notifyPolicyParamVo);
		}
		configObj.put("paramList", paramList);
		notifyPolicyVo.setConfig(configObj.toJSONString());
		JSONObject resultObj = new JSONObject();
		resultObj.put("paramList", paramList);
		return resultObj;
	}
}
