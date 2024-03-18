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

package neatlogic.module.tenant.api.groupsearch;

import neatlogic.framework.auth.core.AuthAction;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

@Deprecated
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class GroupSearchListApi extends PrivateApiComponentBase {
	@Override
	public String getToken() {
		return "groupsearch/list";
	}

	@Override
	public String getName() {
		return "获取组搜索类型接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		
	})
	@Output({
		@Param(name = "value", type=ApiParamType.STRING, desc = "值"),
		@Param(name = "text", type=ApiParamType.STRING, desc = "显示文本")
	})
	@Description(desc = "获取组搜索类型接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONArray jsonArray = new JSONArray();
		for(GroupSearch gs : GroupSearch.values()) {
			JSONObject json = new JSONObject();
			json.put("text", gs.getText());
			json.put("value", gs.getValue());
			jsonArray.add(json);
		}
		return jsonArray;
	}
}
