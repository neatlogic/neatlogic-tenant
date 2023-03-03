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

package neatlogic.module.tenant.api.notify;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.notify.core.NotifyHandlerFactory;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyHandlerListApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "notify/handler/list";
	}

	@Override
	public String getName() {
		return "通知插件列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Output({
		@Param(explode = ValueTextVo[].class, desc = "通知插件列表")
	})
	@Description(desc = "通知插件列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return NotifyHandlerFactory.getNotifyHandlerTypeList();
	}

}
