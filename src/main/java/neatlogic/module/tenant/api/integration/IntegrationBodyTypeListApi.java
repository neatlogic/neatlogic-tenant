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

package neatlogic.module.tenant.api.integration;

import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.integration.authentication.enums.BodyType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

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
