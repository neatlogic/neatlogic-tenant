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

import java.util.ArrayList;
import java.util.List;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.auth.label.INTERFACE_MODIFY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.dao.mapper.ApiMapper;
import neatlogic.framework.restful.dto.ApiVo;

@Service
@Transactional
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class ApiManageBatchUpdateApi extends PrivateApiComponentBase {
	
	@Autowired
	private ApiMapper ApiMapper;
	
	@Override
	public String getToken() {
		return "apimanage/batch/update";
	}

	@Override
	public String getName() {
		return "接口批量设置接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "tokenList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "token数组"),
		@Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "是否激活"),
		@Param(name = "timeout", type = ApiParamType.INTEGER, desc = "请求时效"),
		@Param(name = "expire", type = ApiParamType.LONG, desc = "使用期限")
	})
	@Description(desc = "接口批量设置接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ApiVo apiVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ApiVo>(){});
		List<String> tokenList = apiVo.getTokenList();
		if(tokenList.isEmpty()) {
			return null;
		}
		List<ApiVo> apiList = ApiMapper.getApiListByTokenList(tokenList);
		
		if(!apiList.isEmpty()) {
			List<String> dbTokenList = new ArrayList<>();
			for(ApiVo api : apiList) {
				dbTokenList.add(api.getToken());
			}
			apiVo.setTokenList(dbTokenList);
			ApiMapper.batchUpdate(apiVo);
			tokenList.removeAll(dbTokenList);
		}
		if(tokenList.isEmpty()) {
			return null;
		}
		for(ApiVo api : PrivateApiComponentFactory.getApiList()) {
			if(!tokenList.contains(api.getToken())) {
				continue;
			}
			boolean isUpdate = false;
			if(!apiVo.getIsActive().equals(api.getIsActive())) {
				api.setIsActive(apiVo.getIsActive());
				isUpdate = true;
			}
			
			if(apiVo.getTimeout() != null && !apiVo.getTimeout().equals(api.getTimeout())) {
				api.setTimeout(apiVo.getTimeout());
				isUpdate = true;
			}
			if(apiVo.getExpire() != null && !apiVo.getExpire().equals(api.getExpire())) {
				api.setExpire(apiVo.getExpire());
				isUpdate = true;
			}
			if(isUpdate) {
				ApiMapper.replaceApi(api);
			}
		}
		
		return null;
	}

}
