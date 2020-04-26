package codedriver.module.tenant.api.integration;

import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dto.IntegrationHandlerVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@IsActived
public class IntegrationHandlerGetApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "integration/handler/get";
	}

	@Override
	public String getName() {
		return "集成信息处理组件获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "handler", type = ApiParamType.STRING, desc = "处理器", isRequired = true) })
	@Output({ @Param(name = "Return", explode = IntegrationHandlerVo.class, desc = "信息处理组件列表") })
	@Description(desc = "集成信息处理组件获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<IntegrationHandlerVo> handlerList = IntegrationHandlerFactory.getHandlerList();
		for (IntegrationHandlerVo handler : handlerList) {
			if (handler.getHandler().equals(jsonObj.getString("handler"))) {
				return handler;
			}
		}
		return null;
	}
}
