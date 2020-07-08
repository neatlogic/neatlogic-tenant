package codedriver.module.tenant.api.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@AuthAction(name = "INTEGRATION_EDIT")
public class IntegrationDeleteApi extends ApiComponentBase {

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "integration/delete";
	}

	@Override
	public String getName() {
		return "集成设置删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "uuid", isRequired = true) })
	@Description(desc = "集成设置删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		integrationMapper.deleteIntegrationByUuid(jsonObj.getString("uuid"));
		return null;
	}
}
