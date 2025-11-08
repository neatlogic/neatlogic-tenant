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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.notify.core.INotifyContentHandler;
import neatlogic.framework.notify.core.NotifyContentHandlerFactory;
import neatlogic.framework.notify.core.NotifyHandlerFactory;
import neatlogic.framework.notify.exception.NotifyContentHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyHandlerNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyContentHandlerMessageAttrListApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "notify/content/handler/messageattr/list";
	}

	@Override
	public String getName() {
		return "获取通知消息属性列表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "handler", type = ApiParamType.STRING,isRequired = true, desc = "通知内容插件"),
			@Param(name = "notifyHandler", type = ApiParamType.STRING, isRequired = true,desc = "通知方式插件")
	})
	@Output({
			@Param(name = "attrList", type = ApiParamType.JSONARRAY,desc = "属性列表(标题、正文等)"),
			@Param(name = "dataColumnList", explode = ValueTextVo[].class,desc = "工单字段")
	})
	@Description(desc = "获取通知消息属性列表")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject result = new JSONObject();
		String handler = jsonObj.getString("handler");
		String notifyHandler = jsonObj.getString("notifyHandler");
		INotifyContentHandler notifyContentHandler = NotifyContentHandlerFactory.getHandler(handler);
		if(notifyContentHandler == null){
			throw new NotifyContentHandlerNotFoundException(handler);
		}
		if(NotifyHandlerFactory.getHandler(notifyHandler) == null){
			throw new NotifyHandlerNotFoundException(notifyHandler);
		}
		result.put("attrList",notifyContentHandler.getMessageAttrList(notifyHandler));
		result.put("dataColumnList",notifyContentHandler.getDataColumnList(notifyHandler));
		return result;
	}
}
