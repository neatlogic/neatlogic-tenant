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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = NoAuth.class)
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
