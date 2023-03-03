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

package neatlogic.module.tenant.api.apimanage;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.INTERFACE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.ApiNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.dao.mapper.ApiMapper;
import neatlogic.framework.restful.dto.ApiVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiManageGetApi extends PrivateApiComponentBase {

	@Autowired
	private ApiMapper ApiMapper;
	
	@Override
	public String getToken() {
		return "apimanage/get";
	}

	@Override
	public String getName() {
		return "接口配置信息获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "token", type = ApiParamType.STRING, isRequired = true, desc = "接口token")
	})
	@Output({
		@Param(name = "Return", explode = ApiVo.class, isRequired = true, desc = "接口配置信息")
	})
	@Description(desc = "接口配置信息获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String token = jsonObj.getString("token");
		ApiVo apiVo = ApiMapper.getApiByToken(token);
		if(apiVo != null) {
			return apiVo;
		}
		apiVo = PrivateApiComponentFactory.getApiByToken(token);
		if(apiVo != null) {
			return apiVo;
		}
		throw new ApiNotFoundException(token);
	}

}
