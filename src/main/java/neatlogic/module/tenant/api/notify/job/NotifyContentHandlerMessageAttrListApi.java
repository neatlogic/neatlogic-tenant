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

package neatlogic.module.tenant.api.notify.job;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.notify.core.INotifyContentHandler;
import neatlogic.framework.notify.core.NotifyContentHandlerFactory;
import neatlogic.framework.notify.core.NotifyHandlerFactory;
import neatlogic.framework.notify.exception.NotifyContentHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyHandlerNotFoundException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service

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
