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
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetUserCountBySelectedUserAndTeamApi extends PrivateApiComponentBase {

	@Resource
	private UserService userService;

	@Override
	public String getToken() {
		return "user/countofselecteduserandteam/get";
	}

	@Override
	public String getName() {
		return "根据选中的用户和分组计算激活的用户数";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "userUuidList", type = ApiParamType.JSONARRAY, desc = "用户uuid"),
			@Param(name = "teamUuidList", type = ApiParamType.JSONARRAY, desc = "分组uuid")
	})
	@Output({ @Param(name = "count", type = ApiParamType.INTEGER, desc = "用户数") })
	@Description(desc = "根据选中的用户和分组计算激活的用户数")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject result = new JSONObject();
		List<String> userUuidList = JSON.parseArray(jsonObj.getString("userUuidList"), String.class);
		List<String> teamUuidList = JSON.parseArray(jsonObj.getString("teamUuidList"), String.class);
		Set<String> uuidList = userService.getUserUuidSetByUserUuidListAndTeamUuidList(userUuidList,teamUuidList);
        result.put("count",uuidList.size());
		return result;
	}
}
