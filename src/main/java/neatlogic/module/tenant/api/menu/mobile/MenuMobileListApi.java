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

package neatlogic.module.tenant.api.menu.mobile;

import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dao.mapper.MenuMobileMapper;
import neatlogic.module.tenant.dto.MenuMobileVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MenuMobileListApi extends PrivateApiComponentBase {
	
	@Autowired
	private MenuMobileMapper menuMobileMapper;

	@Override
	public String getToken() {
		return "menu/mobile/list";
	}

	@Override
	public String getName() {
		return "查询移动端菜单";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		})
	@Output({ 
		@Param(name = "list", explode = MenuMobileVo[].class, desc = "菜单列表")
		})
	@Description(desc = "查询移动端菜单")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return menuMobileMapper.getMenuMobileList();
	}
}
