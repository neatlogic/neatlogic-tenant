package codedriver.module.tenant.api.integration;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.config.Config;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.module.framework.integration.handler.TestInvoker;
import codedriver.framework.integration.dto.IntegrationInvokeVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.auth.label.INTERFACE_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationInvokeApi extends PrivateApiComponentBase {


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
		System.out.println(Config.ES_ENABLE());
		String integrationUuid = jsonObj.getString("uuid");
		String key = jsonObj.getString("key");
		IntegrationInvokeVo integrationInvokeVo = new IntegrationInvokeVo(integrationUuid, new TestInvoker(key));
		//integrationMapper.replaceIntegrationInvoke(integrationInvokeVo);
		return integrationInvokeVo.getDetail();
	}
}
