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

package neatlogic.module.tenant.api.user;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserDataVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

@OperationType(type = OperationTypeEnum.CREATE)
public class UserDataSaveApi extends PrivateApiComponentBase {

	@Autowired
	UserMapper userMapper;


	@Override
	public String getToken() {
		return "user/data/save";
	}

	@Override
	public String getName() {
		return "保存用户数据";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "type", type = ApiParamType.STRING, desc = "功能类型，如果是用户默认模块数据，则应指定为defaultModulePage",
			isRequired = true)
	})
	@Output({})
	@Description(desc = "保存用户数据")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		UserDataVo userDataVo = new UserDataVo();
		String userUuid = UserContext.get().getUserUuid(true);
		String type = jsonObj.getString("type");
		userDataVo.setUserUuid(userUuid);
		userDataVo.setData(jsonObj.toJSONString());
		userDataVo.setType(type);

		if(userMapper.getUserDataByUserUuidAndType(userUuid,type) == null){
			userMapper.insertUserData(userDataVo);
		}else{
			userMapper.updateUserData(userDataVo);
		}
		return null;
	}
}
