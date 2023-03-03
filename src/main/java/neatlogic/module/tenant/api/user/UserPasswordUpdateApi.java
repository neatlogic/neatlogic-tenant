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
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.exception.user.UserCurrentPasswordException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional

@OperationType(type = OperationTypeEnum.UPDATE)
public class UserPasswordUpdateApi extends PrivateApiComponentBase {
	
	@Autowired
	UserMapper userMapper;

	@Override
	public String getToken() {
		return "user/password/update";
	}

	@Override
	public String getName() {
		return "修改用户密码接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "password",
					type = ApiParamType.STRING,
					desc = "用户新密码",
					isRequired = true),
			@Param(name = "oldPassword",
			type = ApiParamType.STRING,
			desc = "用户当前密码",
			isRequired = true)
			
	})
	@Output({})
	@Description(desc = "修改用户密码接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String password = jsonObj.getString("password");
		String oldPassword = jsonObj.getString("oldPassword");
		String userUuid = UserContext.get().getUserUuid(true);
		UserVo user = userMapper.getUserBaseInfoByUuid(userUuid);
		UserVo oldUserVo = new UserVo();
		oldUserVo.setUuid(userUuid);
		oldUserVo.setUserId(user.getUserId());
		oldUserVo.setPassword(oldPassword);
		UserVo userVo = userMapper.getUserByUserIdAndPassword(oldUserVo);
		if(userVo != null) {
			userVo.setPassword(password);		
			userMapper.updateUserPasswordActive(userUuid);
			List<Long> idList = userMapper.getLimitUserPasswordIdList(userUuid);
			if (idList != null && idList.size() > 0){
				userMapper.deleteUserPasswordByLimit(userUuid, idList);
			}
			userMapper.insertUserPassword(userVo);
		}else {
			throw new UserCurrentPasswordException();
		}
		return null;
	}
}
