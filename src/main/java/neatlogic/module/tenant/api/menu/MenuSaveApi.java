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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dto.MenuVo;
import neatlogic.module.tenant.exception.menu.MenuSaveException;
import neatlogic.module.tenant.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

@OperationType(type = OperationTypeEnum.CREATE)
public class MenuSaveApi extends PrivateApiComponentBase {

	@Autowired
	private MenuService menuService;

	@Override
	public String getToken() {
		return "menu/save";
	}

	@Override
	public String getName() {
		return "保存菜单接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "id",
					type = ApiParamType.LONG,
					desc = "菜单id",
					isRequired = false),
			@Param(name = "parentId",
					type = ApiParamType.LONG,
					desc = "父节点id",
					isRequired = true),
			@Param(name = "name",
					type = ApiParamType.STRING,
					desc = "菜单名称",
					isRequired = true),
			@Param(name = "url",
					type = ApiParamType.STRING,
					desc = "菜单url",
					isRequired = true),
			@Param(name = "description",
					type = ApiParamType.STRING,
					desc = "菜单描述",
					isRequired = true),
			@Param(name = "module",
					type = ApiParamType.STRING,
					desc = "模块名",
					isRequired = true),
			@Param(name = "isActive",
					type = ApiParamType.LONG,
					desc = "是否启用，0:正常，1:禁用",
					isRequired = true),
			@Param(name = "isAuto",
					type = ApiParamType.LONG,
					desc = "是否自动打开，0:否，1:是",
					isRequired = true),
			@Param(name = "openMode",
					type = ApiParamType.STRING,
					desc = "打开页面方式，tab:打开新tab页面   blank:打开新标签页",
					isRequired = true),
			@Param(name = "icon",
					type = ApiParamType.STRING,
					desc = "目录对应的图标class",
					isRequired = true),
			@Param(name = "authorityList", type = ApiParamType.JSONARRAY, desc = "授权对象，可多选，格式[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\"]")
			})
	@Output({
			@Param(name = "id",
					type = ApiParamType.LONG,
					desc = "菜单id") })
	@Description(desc = "保存菜单接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject jsonObject = new JSONObject();
		MenuVo menuVo = new MenuVo();
		menuVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<MenuVo>() {
		});
		if (menuVo.getId() == menuVo.getParentId()) {
			throw new MenuSaveException();
		}
		menuService.saveMenu(menuVo);
		jsonObject.put("id", menuVo.getId());
		return jsonObject;
	}
}
