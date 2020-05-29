package codedriver.module.tenant.api.notify;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
public class NotifyPolicyVariableListSaveApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "notify/policy/variablelist/save";
	}

	@Override
	public String getName() {
		return "通知策略变量列表保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "policyUuid", type = ApiParamType.STRING, isRequired = true, desc = "策略uuid"),
		@Param(name = "variableList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "变量列表")
	})
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
