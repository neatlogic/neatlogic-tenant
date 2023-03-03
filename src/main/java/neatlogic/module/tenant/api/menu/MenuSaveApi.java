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
