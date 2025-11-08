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

package neatlogic.module.tenant.api.team;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Deprecated
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TeamListApi extends PrivateApiComponentBase {
	@Autowired
	private TeamMapper teamMapper;
	
	@Override
	public String getToken() {
		return "team/list";
	}

	@Override
	public String getName() {
		return "分组列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字模糊查询", xss = true),
		@Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "用于回显的参数列表"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页展示数量 默认10"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页")
		})
	@Output({
		@Param(name="text", type = ApiParamType.STRING, desc="组名"),
		@Param(name="value", type = ApiParamType.STRING, desc="组uuid")
	})
	@Description(desc = "分组列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		TeamVo teamVo = JSON.toJavaObject(jsonObj, TeamVo.class);
		if(StringUtils.isNotBlank(teamVo.getKeyword())) {
			if (teamVo.getNeedPage()) {
				int rowNum = teamMapper.searchTeamCount(teamVo);
				teamVo.setRowNum(rowNum);
				teamVo.setPageCount(PageUtil.getPageCount(rowNum, teamVo.getPageSize()));
			}
			return teamMapper.searchTeam(teamVo);
		}else {//回显
			JSONArray defaultValue = teamVo.getDefaultValue();
			if(CollectionUtils.isNotEmpty(defaultValue)) {
				return teamMapper.getTeamByUuidList(defaultValue.toJavaList(String.class));
			}
		}
		return null;
	}
}
