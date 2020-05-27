package codedriver.module.tenant.api.integration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@IsActived
public class IntegrationSearchApi extends ApiComponentBase {

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "integration/search";
	}

	@Override
	public String getName() {
		return "集成设置查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字"), @Param(name = "handler", type = ApiParamType.STRING, desc = "组件"), 
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"), 
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数量") })
	@Output({ @Param(explode = BasePageVo.class), @Param(name = "integrationList", explode = IntegrationVo[].class, desc = "集成设置列表") })
	@Description(desc = "集成设置查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		IntegrationVo integrationVo = JSONObject.toJavaObject(jsonObj, IntegrationVo.class);
		List<IntegrationVo> integrationList = integrationMapper.searchIntegration(integrationVo);
		if (integrationList.size() > 0) {
			int rowNum = integrationMapper.searchIntegrationCount(integrationVo);
			integrationVo.setRowNum(rowNum);
			integrationVo.setPageCount(PageUtil.getPageCount(rowNum, integrationVo.getPageSize()));
		}
		JSONObject returnObj = new JSONObject();
		returnObj.put("pageSize", integrationVo.getPageSize());
		returnObj.put("currentPage", integrationVo.getCurrentPage());
		returnObj.put("rowNum", integrationVo.getRowNum());
		returnObj.put("pageCount", integrationVo.getPageCount());
		returnObj.put("tbodyList", integrationList);
		return returnObj;
	}
}
