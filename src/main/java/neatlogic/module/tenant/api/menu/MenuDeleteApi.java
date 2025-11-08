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

package neatlogic.module.tenant.api.menu;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.exception.menu.MenuHasChildrenException;
import neatlogic.module.tenant.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = NoAuth.class)
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
