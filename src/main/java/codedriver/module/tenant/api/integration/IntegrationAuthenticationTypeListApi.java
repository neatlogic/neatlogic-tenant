package codedriver.module.tenant.api.integration;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.integration.authentication.costvalue.AuthenticateType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Deprecated
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationAuthenticationTypeListApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "integration/authenticationtype/list";
	}

	@Override
	public String getName() {
		return "集成配置认证类型列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Output({ @Param(name = "value", type = ApiParamType.STRING, desc = "类型"), @Param(name = "text", type = ApiParamType.STRING, desc = "类型中文名") })
	@Description(desc = "集成配置认证类型列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONArray returnList = new JSONArray();
		for (AuthenticateType type : AuthenticateType.values()) {
			JSONObject j = new JSONObject();
			j.put("value", type.getValue());
			j.put("text", type.getText());
			returnList.add(j);
		}
		return returnList;
	}
}
