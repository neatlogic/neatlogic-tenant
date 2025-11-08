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

package neatlogic.module.tenant.api.user;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserDataVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = NoAuth.class)
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
		return "nmtau.userdatasaveapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "type", type = ApiParamType.STRING, isRequired = true, desc = "common.type", help = "如果是用户默认模块数据，则应指定为defaultModulePage")
	})
	@Output({})
	@Description(desc = "nmtau.userdatasaveapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		UserDataVo userDataVo = new UserDataVo();
		String userUuid = UserContext.get().getUserUuid(true);
		String type = jsonObj.getString("type");
		userDataVo.setUserUuid(userUuid);
		userDataVo.setData(jsonObj);
		userDataVo.setType(type);

		if(userMapper.getUserDataByUserUuidAndType(userUuid,type) == null){
			userMapper.insertUserData(userDataVo);
		}else{
			userMapper.updateUserData(userDataVo);
		}
		return null;
	}
}
