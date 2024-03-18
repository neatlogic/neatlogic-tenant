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

import java.util.ArrayList;
import java.util.List;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.UserMapper;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class UserListApi extends PrivateApiComponentBase {

	@Autowired
	private UserMapper userMapper;
	
	@Override
	public String getToken() {
		return "user/list";
	}

	@Override
	public String getName() {
		return "用户列表获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "teamUuidList", type = ApiParamType.JSONARRAY, desc = "用户组uuid集合"),
		@Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "用户是否激活")
	})
	@Output({
		@Param(name = "userList", type = ApiParamType.JSONARRAY, desc = "用户集合")
	})
	@Description(desc = "用户列表获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<String> teamUuidList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("teamUuidList")), String.class);
		Integer isActive = jsonObj.getInteger("isActive");
		if(CollectionUtils.isNotEmpty(teamUuidList)) {
			List<String> userUuidList = userMapper.getUserUuidListByTeamUuidList(teamUuidList);
			if(CollectionUtils.isNotEmpty(userUuidList)) {
				return userMapper.getUserListByUserUuidList(userUuidList,isActive);
			}
		}
		return new ArrayList<>();
	}

}
