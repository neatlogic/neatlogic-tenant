package codedriver.module.tenant.api.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.integration.core.TestInvoker;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationInvokeVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@IsActived
@AuthAction(name = "INTEGRATION_EDIT")
public class IntegrationInvokeApi extends ApiComponentBase {

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "integration/invoke";
	}

	@Override
	public String getName() {
		return "集成设置引用接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "uuid", isRequired = true), @Param(name = "key", type = ApiParamType.STRING, desc = "key", isRequired = true) })
	@Description(desc = "集成设置引用接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String integrationUuid = jsonObj.getString("uuid");
		String key = jsonObj.getString("key");
		IntegrationInvokeVo integrationInvokeVo = new IntegrationInvokeVo(integrationUuid, new TestInvoker(key));
		//integrationMapper.replaceIntegrationInvoke(integrationInvokeVo);
		return integrationInvokeVo.getDetail();
	}
}
