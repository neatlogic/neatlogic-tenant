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

package neatlogic.module.tenant.api.apimanage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.INTERFACE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentFactory;
import neatlogic.framework.restful.dto.ApiHandlerVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiManageApiHandlerListApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "apimanage/apihandler/list";
	}

	@Override
	public String getName() {
		return "接口组件列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "关键字，接口组件名模糊查询"), 
		@Param(name = "isPrivate", type = ApiParamType.BOOLEAN, desc = "是否是私有接口"), 
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页码，默认值1"), 
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "页大小，默认值10"), 
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页，默认值true")
	})
	@Output({ 
		@Param(explode = BasePageVo.class), 
		@Param(name = "tbodyList", explode = ApiHandlerVo[].class, isRequired = true, desc = "接口组件列表") 
	})
	@Description(desc = "接口组件列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		BasePageVo basePageVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<BasePageVo>() { });

		List<ApiHandlerVo> apiHandlerList = new ArrayList<>();
		String keyword = jsonObj.getString("keyword");
		Boolean isPrivate = jsonObj.getBoolean("isPrivate");
		 List<ApiHandlerVo> apiHandlerFactoryList = PrivateApiComponentFactory.getApiHandlerList();
		if(!isPrivate) {
		    apiHandlerFactoryList = PublicApiComponentFactory.getApiHandlerList();
		}
		for (ApiHandlerVo apiHandlerVo : apiHandlerFactoryList) {
			if (StringUtils.isNotBlank(keyword) && !apiHandlerVo.getName().contains(keyword)) {
				continue;
			}
			if (isPrivate != null && apiHandlerVo.isPrivate() != isPrivate) {
				continue;
			}
			apiHandlerList.add(apiHandlerVo);
		}

		apiHandlerList.sort((apiHandler1, apiHandler2) -> apiHandler1.getHandler().compareTo(apiHandler2.getHandler()));

		JSONObject resultObj = new JSONObject();
		if (basePageVo.getNeedPage()) {
			int rowNum = apiHandlerList.size();
			int pageCount = PageUtil.getPageCount(rowNum, basePageVo.getPageSize());
			resultObj.put("rowNum", rowNum);
			resultObj.put("pageCount", pageCount);
			resultObj.put("pageSize", basePageVo.getPageSize());
			resultObj.put("currentPage", basePageVo.getCurrentPage());
			int fromIndex = basePageVo.getStartNum();
			if(fromIndex < rowNum) {
				int toIndex = fromIndex + basePageVo.getPageSize();
				toIndex = toIndex > rowNum ? rowNum : toIndex;
				apiHandlerList = apiHandlerList.subList(basePageVo.getStartNum(), toIndex);
			}else {
				apiHandlerList = new ArrayList<>();
			}
		}

		resultObj.put("tbodyList", apiHandlerList);

		return resultObj;

	}

}
