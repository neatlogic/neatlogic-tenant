/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.test;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;

public class ParamTypeValidateApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "param/type/validate";
	}

	@Override
	public String getName() {
		return "测试入参类型验证接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "boolParam", type = ApiParamType.BOOLEAN, isRequired = true, desc = "布尔类型"),
		@Param(name = "emailParam", type = ApiParamType.EMAIL, isRequired = true, desc = "email类型"),
		@Param(name = "enumParam", type = ApiParamType.ENUM, isRequired = true, desc = "枚举类型（0或1）", rule = "0,1"),
		@Param(name = "intParam", type = ApiParamType.INTEGER, isRequired = true, desc = "整型"),
		@Param(name = "ipParam", type = ApiParamType.IP, isRequired = true, desc = "IP地址类型"),
		@Param(name = "arrayParam", type = ApiParamType.JSONARRAY, isRequired = true, desc = "json数组类型"),
		@Param(name = "objectParam", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "json对象类型"),
		@Param(name = "longParam", type = ApiParamType.LONG, isRequired = true, desc = "long类型"),
		@Param(name = "regexParam", type = ApiParamType.REGEX, isRequired = true, desc = "正则表达式类型只能输入英文字母", rule = "^[A-Za-z]+$"),
		@Param(name = "stringParam", type = ApiParamType.STRING, isRequired = true, desc = "字符串类型")
	})
	@Output({
		@Param(name = "Reture", type = ApiParamType.JSONOBJECT, desc = "返回输入参数")
	})
	@Description(desc = "测试入参类型验证接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
//		boolean boolParam = jsonObj.getBooleanValue("boolParam");
//		String emailParam = jsonObj.getString("emailParam");
//		String enumParam = jsonObj.getString("enumParam");
//		int intParam = jsonObj.getIntValue("intParam");
//		String ipParam = jsonObj.getString("ipParam");
//		JSONArray arrayParam = jsonObj.getJSONArray("arrayParam");
//		JSONObject objectParam = jsonObj.getJSONObject("objectParam");
//		long longParam = jsonObj.getLongValue("longParam");
//		String regexParam = jsonObj.getString("regexParam");
//		String stringParam = jsonObj.getString("stringParam");
		return jsonObj;
	}

}
