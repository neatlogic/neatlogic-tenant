package codedriver.module.tenant.api.inform;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.inform.dao.mapper.InformTemplateMapper;
import codedriver.framework.inform.dto.InformTemplateVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class InformTemplateSearchApi extends ApiComponentBase {

	@Autowired
	private InformTemplateMapper informTemplateMapper;
	
	@Override
	public String getToken() {
		return "inform/template/search";
	}

	@Override
	public String getName() {
		return "通知模板列表查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name="keyword",type=ApiParamType.STRING,isRequired=false,desc="模板名称模糊匹配"),
		@Param(name="type",type=ApiParamType.STRING,isRequired=false,desc="类型"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=false,desc="当前页码"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=false,desc="页大小")
		})
	@Output({
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=true,desc="当前页码"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=true,desc="页大小"),
		@Param(name="pageCount",type=ApiParamType.INTEGER,isRequired=true,desc="总页数"),
		@Param(name="rowNum",type=ApiParamType.INTEGER,isRequired=true,desc="总行数"),
		@Param(name="informTemplateList",explode = InformTemplateVo[].class, desc="通知模板列表")
	})
	@Description(desc = "通知模板列表查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		InformTemplateVo informTemplateVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<InformTemplateVo>() {});
		if(informTemplateVo.getNeedPage()) {
			int rowNum = informTemplateMapper.searchInformTemplateCount(informTemplateVo);
			int pageCount = PageUtil.getPageCount(rowNum, informTemplateVo.getPageSize());
			resultObj.put("currentPage", informTemplateVo.getCurrentPage());
			resultObj.put("pageSize", informTemplateVo.getPageSize());
			resultObj.put("pageCount", pageCount);
			resultObj.put("rowNum", rowNum);
		}
		
		List<InformTemplateVo> informTemplateList = informTemplateMapper.searchInformTemplateList(informTemplateVo);
		resultObj.put("informTemplateList", informTemplateList);
		return resultObj;
	}

}
