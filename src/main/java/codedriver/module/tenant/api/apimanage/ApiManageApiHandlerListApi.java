package codedriver.module.tenant.api.apimanage;

import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.restful.core.ApiComponentFactory;
import codedriver.framework.restful.dto.ApiHandlerVo;
@Service
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
	
	@Input({
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc="当前页码，默认值1"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc="页大小，默认值10"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc="是否分页，默认值true")
		
	})
	@Output({
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc="当前页码"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "页大小"),
		@Param(name = "pageCount", type = ApiParamType.INTEGER, desc = "总页数"),
		@Param(name = "rowNum", type = ApiParamType.INTEGER, desc = "总行数"),
		@Param(name = "apiHandlerList", explode = ApiHandlerVo[].class, isRequired = true, desc = "接口组件列表")
	})
	@Description(desc = "接口组件列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		BasePageVo basePageVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<BasePageVo>() {});
		
		List<ApiHandlerVo> apiHandlerList = ApiComponentFactory.getApiHandlerList();
		apiHandlerList.sort((apiHandler1, apiHandler2) -> apiHandler1.getHandler().compareTo(apiHandler2.getHandler()));
		
		JSONObject resultObj = new JSONObject();
		if(basePageVo.getNeedPage()) {
			int rowNum = apiHandlerList.size();
			int pageCount = PageUtil.getPageCount(rowNum, basePageVo.getPageSize());
			resultObj.put("rowNum", rowNum);
			resultObj.put("pageCount", pageCount);
			resultObj.put("pageSize", basePageVo.getPageSize());
			resultObj.put("currentPage", basePageVo.getCurrentPage());
			apiHandlerList = apiHandlerList.subList(basePageVo.getStartNum(), basePageVo.getStartNum() + basePageVo.getPageSize());
		}
		
		resultObj.put("apiHandlerList", apiHandlerList);
		
		return resultObj;
				
	}

}
