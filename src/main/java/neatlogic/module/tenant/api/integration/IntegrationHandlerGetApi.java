/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.integration;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.integration.core.IntegrationHandlerFactory;
import neatlogic.framework.integration.dto.IntegrationHandlerVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

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
