/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.integration;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.integration.core.IntegrationHandlerFactory;
import neatlogic.framework.integration.dto.IntegrationHandlerVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationHandlerGetApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "integration/handler/get";
	}

	@Override
	public String getName() {
		return "集成信息处理组件获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "handler", type = ApiParamType.STRING, desc = "处理器", isRequired = true) })
	@Output({ @Param(name = "Return", explode = IntegrationHandlerVo.class, desc = "信息处理组件列表") })
	@Description(desc = "集成信息处理组件获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<IntegrationHandlerVo> handlerList = IntegrationHandlerFactory.getHandlerList();
		for (IntegrationHandlerVo handler : handlerList) {
			if (handler.getHandler().equals(jsonObj.getString("handler"))) {
				return handler;
			}
		}
		return null;
	}
}
