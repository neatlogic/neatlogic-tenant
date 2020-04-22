package codedriver.module.tenant.api.apimanage;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.restful.core.ApiComponentFactory;
import codedriver.framework.restful.dto.ApiHandlerVo;

@Service
@IsActived
public class ApiManageApiHandlerListApi extends ApiComponentBase {

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

	@Input({ @Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "关键字，接口组件名模糊查询"), @Param(name = "isPrivate", type = ApiParamType.BOOLEAN, desc = "是否是私有接口"), @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页码，默认值1"), @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "页大小，默认值10"), @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页，默认值true")

	})
	@Output({ @Param(explode = BasePageVo.class), @Param(name = "tbodyList", explode = ApiHandlerVo[].class, isRequired = true, desc = "接口组件列表") })
	@Description(desc = "接口组件列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		BasePageVo basePageVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<BasePageVo>() {
		});

		List<ApiHandlerVo> apiHandlerList = new ArrayList<>();
		String keyword = jsonObj.getString("keyword");
		Boolean isPrivate = jsonObj.getBoolean("isPrivate");
		for (ApiHandlerVo apiHandlerVo : ApiComponentFactory.getApiHandlerList()) {
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
			int endNum = basePageVo.getStartNum() + basePageVo.getPageSize();
			endNum = endNum < rowNum ? endNum : rowNum;
			apiHandlerList = apiHandlerList.subList(basePageVo.getStartNum(), basePageVo.getStartNum() + basePageVo.getPageSize());
		}

		resultObj.put("tbodyList", apiHandlerList);

		return resultObj;

	}

}
