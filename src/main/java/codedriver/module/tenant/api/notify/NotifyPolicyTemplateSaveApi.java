package codedriver.module.tenant.api.notify;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
public class NotifyPolicyTemplateSaveApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "notify/policy/template/save";
	}

	@Override
	public String getName() {
		return "通知模板保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "policyUuid", type = ApiParamType.STRING, isRequired = true, desc = "策略uuid"),
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "模板uuid"),
		@Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "模板名称"),
		@Param(name = "title", type = ApiParamType.STRING, isRequired = true, desc = "模板标题"),
		@Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "模板内容")
	})
	@Output({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "模板uuid")
	})
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
