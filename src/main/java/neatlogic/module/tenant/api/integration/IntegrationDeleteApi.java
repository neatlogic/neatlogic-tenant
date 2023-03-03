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
import neatlogic.framework.dependency.constvalue.FrameworkFromType;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.exception.integration.IntegrationReferencedCannotBeDeletedException;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.auth.label.INTERFACE_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class IntegrationDeleteApi extends PrivateApiComponentBase {

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "integration/delete";
	}

	@Override
	public String getName() {
		return "集成设置删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "uuid", isRequired = true) })
	@Description(desc = "集成设置删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		if(DependencyManager.getDependencyCount(FrameworkFromType.INTEGRATION, uuid) > 0){
			throw new IntegrationReferencedCannotBeDeletedException(uuid);
		}
		integrationMapper.deleteIntegrationByUuid(uuid);
		return null;
	}
}
