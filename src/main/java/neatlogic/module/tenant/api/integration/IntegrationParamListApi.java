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

package neatlogic.module.tenant.api.integration;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.integration.IntegrationHandlerNotFoundException;
import neatlogic.framework.integration.core.IIntegrationHandler;
import neatlogic.framework.integration.core.IntegrationHandlerFactory;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.integration.dto.IntegrationVo;
import neatlogic.framework.integration.dto.PatternVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationParamListApi extends PrivateApiComponentBase {

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "integration/param/list";
	}

	@Override
	public String getName() {
		return "获取集成设置参数列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "集成配置uuid", isRequired = true), @Param(name = "mode", type = ApiParamType.ENUM, rule = "input,output", desc = "参数模式，input或output") })
	@Output({ @Param(explode = PatternVo[].class) })
	@Description(desc = "获取集成设置参数列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String mode = jsonObj.getString("mode");
		IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(jsonObj.getString("uuid"));
		JSONObject configObj = integrationVo.getConfig();
		IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
		if (handler == null) {
			throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
		}
		List<PatternVo> patternList = new ArrayList<>();
		if (handler.hasPattern().equals(0)) {
			if (configObj.getJSONObject("param") != null && configObj.getJSONObject("param").getJSONArray("paramList") != null) {
				for (int i = 0; i < configObj.getJSONObject("param").getJSONArray("paramList").size(); i++) {
					JSONObject paramObj = configObj.getJSONObject("param").getJSONArray("paramList").getJSONObject(i);
					PatternVo patternVo = JSONObject.toJavaObject(paramObj, PatternVo.class);
					if (StringUtils.isBlank(mode) || mode.equals(patternVo.getMode())) {
						patternList.add(patternVo);
					}
				}
			}
		} else {
			if (StringUtils.isBlank(mode)) {
				if (handler.getInputPattern() != null) {
					patternList.addAll(handler.getInputPattern());
				}
				if (handler.getOutputPattern() != null) {
					patternList.addAll(handler.getOutputPattern());
				}
			} else if (mode.equals("input")) {
				if (handler.getInputPattern() != null) {
					patternList.addAll(handler.getInputPattern());
				}
			} else if (mode.equals("output")) {
				if (handler.getOutputPattern() != null) {
					patternList.addAll(handler.getOutputPattern());
				}
			}
		}
		return patternList;
	}
}
