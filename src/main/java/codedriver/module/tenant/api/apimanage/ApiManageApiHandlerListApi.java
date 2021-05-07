/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.apimanage;

import java.util.ArrayList;
import java.util.List;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentFactory;
import codedriver.framework.restful.core.publicapi.PublicApiComponentFactory;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.restful.dto.ApiHandlerVo;

@Service

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
