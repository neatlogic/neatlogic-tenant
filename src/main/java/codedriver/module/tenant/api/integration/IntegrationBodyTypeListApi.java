package codedriver.module.tenant.api.integration;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.integration.authentication.costvalue.BodyType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Deprecated
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationBodyTypeListApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "integration/bodytype/list";
	}

	@Override
	public String getName() {
		return "集成配置请求体类型列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Output({ @Param(name = "value", type = ApiParamType.STRING, desc = "值"), @Param(name = "text", type = ApiParamType.STRING, desc = "显示文本") })
	@Description(desc = "集成配置请求体类型列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONArray returnList = new JSONArray();
		for (BodyType t : BodyType.values()) {
			JSONObject p = new JSONObject();
			p.put("value", t.toString());
			p.put("text", t.toString());
			returnList.add(p);
		}
		return returnList;
	}
}
