package codedriver.module.tenant.api.integration;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
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

	@Input({ 
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字"), 
		@Param(name = "valueList", type = ApiParamType.JSONARRAY, desc = "回显值"), 
		@Param(name = "handler", type = ApiParamType.STRING, desc = "组件"), 
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
		//补充类型对应表达式信息
		if(CollectionUtils.isNotEmpty(integrationList)) {
			for(IntegrationVo inte : integrationList) {
				JSONObject paramJson = inte.getConfig().getJSONObject("param");
				if(paramJson != null) {
					JSONArray paramList = paramJson.getJSONArray("paramList");
					if(CollectionUtils.isNotEmpty(paramList)) {
						for(Object paramObj:paramList) {
							JSONObject param = (JSONObject)paramObj;
							ParamType pt = ParamType.getParamType(param.getString("type"));
							if(pt != null) {
								//增加参数回显模版-赖文韬-202006291121
								String freemarkerTemplate = pt.getFreemarkerTemplate(param.getString("name"));
								param.put("freemarkerTemplate",freemarkerTemplate);
								param.put("expresstionList", pt.getExpressionJSONArray());
							}
						}
					}
				}
			}
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
