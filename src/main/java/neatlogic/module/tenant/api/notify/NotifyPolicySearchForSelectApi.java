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

package neatlogic.module.tenant.api.notify;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicySearchForSelectApi extends PrivateApiComponentBase {
	
	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/policy/search/forselect";
	}

	@Override
	public String getName() {
		return "查询通知策略管理列表_下拉框";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字搜索"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数"),
		@Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "通知策略处理器")
	})
	@Output({
		@Param(name = "list", explode = ValueTextVo[].class, desc = "通知策略列表")
	})
	@Description(desc = "查询通知策略管理列表_下拉框")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		NotifyPolicyVo notifyPolicyVo = JSON.toJavaObject(jsonObj, NotifyPolicyVo.class);
		List<ValueTextVo> tbodyList = notifyMapper.getNotifyPolicyListForSelect(notifyPolicyVo);

		resultObj.put("list", tbodyList);
		if(notifyPolicyVo.getNeedPage()) {
			int rowNum = notifyMapper.getNotifyPolicyCount(notifyPolicyVo);
			resultObj.put("currentPage", notifyPolicyVo.getCurrentPage());
			resultObj.put("pageSize", notifyPolicyVo.getPageSize());
			resultObj.put("pageCount", PageUtil.getPageCount(rowNum, notifyPolicyVo.getPageSize()));
			resultObj.put("rowNum", rowNum);
		}
		return resultObj;
	}

}
