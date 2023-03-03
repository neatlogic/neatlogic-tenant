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
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.auth.label.INTERFACE_MODIFY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.ApiNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.dao.mapper.ApiMapper;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.module.tenant.exception.api.ApiNotAllowedToDeleteException;

@Service
@Transactional
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class ApiManageDeleteApi extends PrivateApiComponentBase {
	
	@Autowired
	private ApiMapper ApiMapper;
	
	@Override
	public String getToken() {
		return "apimanage/delete";
	}

	@Override
	public String getName() {
		return "接口删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "token", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "接口token")
	})
	@Description(desc = "接口删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String token = jsonObj.getString("token");
		ApiVo apiVo = PrivateApiComponentFactory.getApiByToken(token);
		//内存中的接口不允许删除
		if(apiVo != null) {
			throw new ApiNotAllowedToDeleteException(token);
		}
		apiVo = ApiMapper.getApiByToken(token);
		if(apiVo == null) {
			throw new ApiNotFoundException(token);
		}
		ApiMapper.deleteApiByToken(token);
		return null;
	}

}
