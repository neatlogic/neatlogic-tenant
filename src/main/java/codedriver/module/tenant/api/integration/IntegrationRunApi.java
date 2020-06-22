package codedriver.module.tenant.api.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.exception.integration.IntegrationNotFoundException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.integration.handler.FrameworkRequestFrom;

@Service
@IsActived
public class IntegrationRunApi extends ApiComponentBase {

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "integration/run/{uuid}";
	}

	@Override
	public String getName() {
		return "集成配置执行接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "集成配置uuid", isRequired = true) })
	@Description(desc = "集成配置执行接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(jsonObj.getString("uuid"));
		if (integrationVo == null) {
			throw new IntegrationNotFoundException(jsonObj.getString("uuid"));
		}
		jsonObj.remove("uuid");
		integrationVo.setParamObj(jsonObj);
		IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
		if (handler == null) {
			throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
		}

		IntegrationResultVo resultVo = handler.sendRequest(integrationVo, FrameworkRequestFrom.API);
		return resultVo;
	}
}
