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

package neatlogic.module.tenant.api.groupsearch;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

@Deprecated
@Service
@AuthAction(action = NoAuth.class)
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
