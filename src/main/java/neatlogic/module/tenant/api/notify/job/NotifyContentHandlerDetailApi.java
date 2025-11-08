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

package neatlogic.module.tenant.api.notify.job;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.ConditionParamVo;
import neatlogic.framework.notify.core.INotifyContentHandler;
import neatlogic.framework.notify.core.NotifyContentHandlerFactory;
import neatlogic.framework.notify.exception.NotifyContentHandlerNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyContentHandlerDetailApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "notify/content/handler/detail";
	}

	@Override
	public String getName() {
		return "获取通知内容插件详情";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "handler", type = ApiParamType.STRING, isRequired = true,desc = "通知内容插件")})
	@Output({
			@Param(name = "conditionList", explode = ConditionParamVo[].class,desc = "条件列表"),
	})
	@Description(desc = "获取通知内容插件详情")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		/** 传入new LinkedHashMap()可按put顺序排序，先渲染条件，再渲染数据列 */
		JSONObject result = new JSONObject(new LinkedHashMap<>());
		String handler = jsonObj.getString("handler");
		INotifyContentHandler notifyContentHandler = NotifyContentHandlerFactory.getHandler(handler);
		if(notifyContentHandler == null){
			throw new NotifyContentHandlerNotFoundException(handler);
		}
		JSONArray conditionList = notifyContentHandler.getConditionOptionList();
		result.put("conditionList",conditionList);
		return result;
	}
}
