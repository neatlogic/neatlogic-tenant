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

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.service.UserService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@Service

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
