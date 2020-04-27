package codedriver.module.tenant.api.integration;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@IsActived
public class IntegrationPatternTypeListApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "integration/patterntype/list";
	}

	@Override
	public String getName() {
		return "集成配置参数类型列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Output({ @Param(name = "value", type = ApiParamType.STRING, desc = "类型"), @Param(name = "text", type = ApiParamType.STRING, desc = "类型中文名") })
	@Description(desc = "集成配置参数类型列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONArray returnList = new JSONArray();
		for (ApiParamType type : ApiParamType.values()) {
			JSONObject j = new JSONObject();
			j.put("value", type.getValue());
			j.put("text", type.getText());
			returnList.add(j);
		}
		return returnList;
	}
}
