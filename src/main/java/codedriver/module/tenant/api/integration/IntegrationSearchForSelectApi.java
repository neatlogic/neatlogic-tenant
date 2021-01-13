package codedriver.module.tenant.api.integration;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationSearchForSelectApi extends PrivateApiComponentBase {

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "integration/search/forselect";
	}

	@Override
	public String getName() {
		return "查询集成设置_下拉框";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ 
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字"), 
		@Param(name = "valueList", type = ApiParamType.JSONARRAY, desc = "回显值"), 
		@Param(name = "handler", type = ApiParamType.STRING, desc = "组件"), 
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"), 
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数量") })
	@Output({@Param(name = "list", explode = ValueTextVo[].class, desc = "集成设置列表") })
	@Description(desc = "查询集成设置_下拉框")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		IntegrationVo integrationVo = JSONObject.toJavaObject(jsonObj, IntegrationVo.class);
		List<ValueTextVo> integrationList = integrationMapper.searchIntegrationForSelect(integrationVo);
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
		returnObj.put("list", integrationList);
		return returnObj;
	}
}
