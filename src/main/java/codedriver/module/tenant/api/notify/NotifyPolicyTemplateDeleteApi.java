package codedriver.module.tenant.api.notify;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
public class NotifyPolicyTemplateDeleteApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "notify/policy/template/delete";
	}

	@Override
	public String getName() {
		return "通知模板删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "policyUuid", type = ApiParamType.STRING, isRequired = true, desc = "策略uuid"),
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "模板uuid")
	})
	@Description(desc = "通知策略信息获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
