package codedriver.module.tenant.api.integration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.integration.ParamFormatInvalidException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.util.FreemarkerUtil;

@Service
@IsActived
public class IntegrationTransformTest extends ApiComponentBase {

	@Override
	public String getToken() {
		return "integration/transformtest";
	}

	@Override
	public String getName() {
		return "集成设置参数转换测试接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "content", type = ApiParamType.STRING, desc = "原始内容，需要符合json格式", isRequired = true), @Param(name = "template", type = ApiParamType.STRING, desc = "转换模板，如果为空则不做转换") })
	@Output({ @Param(name = "Return", type = ApiParamType.STRING, desc = "返回结果") })
	@Description(desc = "集成设置参数转换测试接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String content = jsonObj.getString("content");
		String template = jsonObj.getString("template");
		Object object = null;
		try {
			object = JSONObject.parseObject(content);
		} catch (Exception ex) {
			try {
				object = JSONArray.parseArray(content);
			} catch (Exception e2) {

			}
		}
		if (object == null) {
			throw new ParamFormatInvalidException();
		}

		String returnStr = FreemarkerUtil.transform(object, template);
		if (StringUtils.isBlank(returnStr)) {
			returnStr = "没有任何内容";
		}
		return returnStr;
	}
}
