/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.notify.job;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.ConditionParamVo;
import neatlogic.framework.notify.core.INotifyContentHandler;
import neatlogic.framework.notify.core.NotifyContentHandlerFactory;
import neatlogic.framework.notify.exception.NotifyContentHandlerNotFoundException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

@Service

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
