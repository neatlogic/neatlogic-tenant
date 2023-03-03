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

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.exception.menu.MenuHasChildrenException;
import neatlogic.module.tenant.service.MenuService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

@OperationType(type = OperationTypeEnum.DELETE)
public class MenuDeleteApi extends PrivateApiComponentBase{

	@Autowired
	private MenuService menuService;
	
	@Override
	public String getToken() {
		return "menu/delete";
	}

	@Override
	public String getName() {
		return "删除菜单接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "id", type = ApiParamType.LONG, desc = "菜单id",isRequired = true) })
	@Output({})
	@Description(desc = "删除菜单接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
        Long menuId = jsonObj.getLong("id"); 
		JSONObject jsonObject = new JSONObject();
		int count = this.menuService.checkIsChildern(menuId);
		if (count > 0) {
			throw new MenuHasChildrenException(count);
		} else {
			this.menuService.deleteMenu(menuId);
		}
		return jsonObject;
	}
}
