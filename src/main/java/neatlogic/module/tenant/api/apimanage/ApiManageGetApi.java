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

package neatlogic.module.tenant.api.apimanage;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.INTERFACE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.ApiNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.dao.mapper.ApiMapper;
import neatlogic.framework.restful.dto.ApiHandlerVo;
import neatlogic.framework.restful.dto.ApiVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiManageGetApi extends PrivateApiComponentBase {

	@Resource
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
		String normalizedToken = token;
		if (normalizedToken != null && normalizedToken.startsWith("/")) {
			normalizedToken = normalizedToken.substring(1);
		}
		if (normalizedToken != null && normalizedToken.endsWith("/")) {
			normalizedToken = normalizedToken.substring(0, normalizedToken.length() - 1);
		}
		ApiVo api = PrivateApiComponentFactory.getApiByToken(normalizedToken);
		ApiVo apiVo = ApiMapper.getApiByTokenWithoutPsw(token);
		if(apiVo != null) {
            if(api != null){
                apiVo.setAuthTypeList(api.getAuthTypeList());
                apiVo.setHandler(api.getHandler());
                apiVo.setModuleId(api.getModuleId());
                apiVo.setType(api.getType());
            } else if (apiVo.getHandler() != null) {
                ApiHandlerVo apiHandlerVo = PrivateApiComponentFactory.getApiHandlerByHandler(apiVo.getHandler());
                if (apiHandlerVo != null) {
                    apiVo.setType(apiHandlerVo.getType());
                    apiVo.setModuleId(apiHandlerVo.getModuleId());
                }
            }
			return apiVo;

		}
		if(api != null) {
			return api;
		}
		throw new ApiNotFoundException(token);
	}

}
