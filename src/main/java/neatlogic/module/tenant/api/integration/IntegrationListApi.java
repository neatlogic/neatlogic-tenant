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

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.integration.dto.IntegrationVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Deprecated
@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationListApi extends PrivateApiComponentBase {

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "integration/list";
	}

	@Override
	public String getName() {
		return "集成设置数据列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "handler", type = ApiParamType.STRING, desc = "组件")
	})
	@Output({ @Param(explode = IntegrationVo[].class, desc = "集成设置列表") })
	@Description(desc = "集成设置数据列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		IntegrationVo integrationVo = new IntegrationVo();
		integrationVo.setNeedPage(false);
		integrationVo.setIsActive(null);
		integrationVo.setHandler(jsonObj.getString("handler"));
		return integrationMapper.searchIntegration(integrationVo);
	}

}
