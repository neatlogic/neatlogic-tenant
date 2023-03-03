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

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.integration.IntegrationHandlerNotFoundException;
import neatlogic.framework.exception.integration.IntegrationNotFoundException;
import neatlogic.framework.integration.core.IIntegrationHandler;
import neatlogic.framework.integration.core.IntegrationHandlerFactory;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.integration.dto.IntegrationVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.auth.label.INTEGRATION_MODIFY;
import neatlogic.module.framework.integration.handler.FrameworkRequestFrom;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = INTEGRATION_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class IntegrationRunApi extends PrivateApiComponentBase {

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "integration/run/{uuid}";
	}

	@Override
	public String getName() {
		return "集成配置执行接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "集成配置uuid", isRequired = true) })
	@Description(desc = "集成配置执行接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(jsonObj.getString("uuid"));
		if (integrationVo == null) {
			throw new IntegrationNotFoundException(jsonObj.getString("uuid"));
		}
		jsonObj.remove("uuid");
		integrationVo.setParamObj(jsonObj);
		IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
		if (handler == null) {
			throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
		}

		return handler.sendRequest(integrationVo, FrameworkRequestFrom.API);
	}
}
