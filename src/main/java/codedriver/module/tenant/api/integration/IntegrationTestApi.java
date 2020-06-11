package codedriver.module.tenant.api.integration;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@IsActived
public class IntegrationTestApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "integration/test";
	}

	@Override
	public String getName() {
		return "集成配置测试接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ 
		@Param(name = "url", type = ApiParamType.STRING, desc = "目标地址", isRequired = true, rule = "^((http|ftp|https)://)(([a-zA-Z0-9\\._-]+)|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?"), 
		@Param(name = "handler", type = ApiParamType.STRING, desc = "组件", isRequired = true, xss = true), 
		@Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "配置，json格式", isRequired = true) 
	})
	@Description(desc = "集成配置测试接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		IntegrationVo integrationVo = JSONObject.toJavaObject(jsonObj, IntegrationVo.class);
		IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
		if (handler == null) {
			throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
		}
		IntegrationResultVo resultVo = handler.sendRequest(integrationVo);
		return resultVo;
	}
}
