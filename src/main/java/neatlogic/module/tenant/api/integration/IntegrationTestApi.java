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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.INTEGRATION_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.exception.integration.IntegrationHandlerNotFoundException;
import neatlogic.framework.integration.core.IIntegrationHandler;
import neatlogic.framework.integration.core.IntegrationHandlerFactory;
import neatlogic.framework.integration.dto.IntegrationResultVo;
import neatlogic.framework.integration.dto.IntegrationVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.framework.integration.handler.FrameworkRequestFrom;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = INTEGRATION_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class IntegrationTestApi extends PrivateApiComponentBase {

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
		IntegrationVo integrationVo = JSON.toJavaObject(jsonObj, IntegrationVo.class);
		IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
		if (handler == null) {
			throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
		}
		IntegrationResultVo resultVo = handler.sendRequest(integrationVo, FrameworkRequestFrom.TEST);
		try {
			handler.validate(resultVo);
		}catch (ApiRuntimeException ex){
			resultVo.appendError(ex.getMessage());
		}
		return resultVo;
	}
}
