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

package neatlogic.module.tenant.api.user;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.exception.user.UserCurrentPasswordException;
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
			if (idList != null && !idList.isEmpty()) {
				userMapper.deleteUserPasswordByLimit(userUuid, idList);
			}
			userMapper.insertUserPassword(userVo);
		}else {
			throw new UserCurrentPasswordException();
		}
		return null;
	}
}
