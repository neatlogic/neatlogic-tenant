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

package neatlogic.module.tenant.api.role;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class RoleSearchForSelectApi extends PrivateApiComponentBase {

	@Autowired
	private RoleMapper roleMapper;

	@Override
	public String getToken() {
		return "role/search/forselect";
	}

	@Override
	public String getName() {
		return "查询角色_下拉";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "keyword",
					type = ApiParamType.STRING,
					desc = "关键字，匹配名称或说明", xss = true),
			@Param(name = "needPage",
					type = ApiParamType.BOOLEAN,
					desc = "是否需要分页，默认true"),
			@Param(name = "pageSize",
					type = ApiParamType.INTEGER,
					desc = "每页条目"),
			@Param(name = "currentPage",
					type = ApiParamType.INTEGER,
					desc = "当前页")})
	@Output({
			@Param(name = "list",
					type = ApiParamType.JSONARRAY,
					explode = ValueTextVo[].class,
					desc = "选项列表"),
			@Param(name = "pageSize",
					type = ApiParamType.INTEGER,
					desc = "每页数据条目"),
			@Param(name = "currentPage",
					type = ApiParamType.INTEGER,
					desc = "当前页"),
			@Param(name = "rowNum",
					type = ApiParamType.INTEGER,
					desc = "返回条目总数"),
			@Param(name = "pageCount",
					type = ApiParamType.INTEGER,
					desc = "总页数")})
	@Description(desc = "查询角色_下拉")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		
		JSONObject resultObj = new JSONObject();
		RoleVo roleVo = JSON.toJavaObject(jsonObj, RoleVo.class);
		if(roleVo.getNeedPage()) {
			int rowNum = roleMapper.searchRoleCount(roleVo);
			resultObj.put("pageSize", roleVo.getPageSize());
			resultObj.put("currentPage", roleVo.getCurrentPage());
			resultObj.put("rowNum", rowNum);
			resultObj.put("pageCount", PageUtil.getPageCount(rowNum, roleVo.getPageSize()));
		}
		resultObj.put("list", roleMapper.searchRoleForSelect(roleVo));
		return resultObj;
	}
}
