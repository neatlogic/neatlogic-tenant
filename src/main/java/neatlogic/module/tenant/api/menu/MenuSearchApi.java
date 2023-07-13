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

package neatlogic.module.tenant.api.menu;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dao.mapper.MenuMapper;
import neatlogic.module.tenant.dto.MenuVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MenuSearchApi extends PrivateApiComponentBase {
	
	@Autowired
	private MenuMapper menuMapper;

	@Override
	public String getToken() {
		return "menu/search";
	}

	@Override
	public String getName() {
		return "查询菜单接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ 
		@Param(name = "id", type = ApiParamType.LONG, desc = "菜单id", isRequired = false), 
		@Param(name = "parentId", type = ApiParamType.LONG, desc = "菜单父节点id", isRequired = false), 
		@Param(name = "type", type = ApiParamType.INTEGER, desc = "默认1，1:返回全部菜单，0:根据角色返回", isRequired = false),
		@Param(name = "isAll", type = ApiParamType.INTEGER, desc = "是否返回所有菜单，默认1，0不启用的菜单不返回，如果父节点不激活，则它所有子节点都不反回；1返回所有菜单。", isRequired = false)
		})
	@Output({ 
		@Param(name = "menuList", explode = MenuVo[].class, desc = "菜单列表")
		})
	@Description(desc = "查询菜单接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject json = new JSONObject();
		List<String> roleUuidList = new ArrayList<String>();
		Integer isActive = null;
		// 如果是根据角色返回对应菜单
		if (jsonObj.containsKey("type") && jsonObj.getIntValue("type") == 0) {
			roleUuidList = UserContext.get().getAuthenticationInfoVo().getRoleUuidList();
		}
		if (jsonObj.containsKey("isAll") &&jsonObj.getIntValue("isAll") == 0) {
			isActive = 1;
		}
		List<MenuVo> menuList = menuMapper.getMenuList(new MenuVo(jsonObj.getLong("id"), jsonObj.getLong("parentId"), isActive, roleUuidList));
		json.put("menuList", menuList);
		return json;
	}
}
