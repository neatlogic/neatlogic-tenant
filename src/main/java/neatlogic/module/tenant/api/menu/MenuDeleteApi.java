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
