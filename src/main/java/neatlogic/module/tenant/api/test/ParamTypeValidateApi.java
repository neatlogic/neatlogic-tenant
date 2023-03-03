/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.test;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
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
		@Param(name = "regexParam", type = ApiParamType.REGEX, isRequired = true, desc = "正则表达式类型只能输入英文字母", rule = RegexUtils.ENGLISH_NAME),
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
