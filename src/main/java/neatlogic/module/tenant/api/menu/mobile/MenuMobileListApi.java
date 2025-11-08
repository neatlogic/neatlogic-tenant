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

package neatlogic.module.tenant.api.menu.mobile;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dao.mapper.MenuMobileMapper;
import neatlogic.module.tenant.dto.MenuMobileVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = NoAuth.class)
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
