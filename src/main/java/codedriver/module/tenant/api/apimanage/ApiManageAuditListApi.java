package codedriver.module.tenant.api.apimanage;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.exception.type.ApiNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.restful.dao.mapper.ApiMapper;
import codedriver.framework.restful.dto.ApiAuditVo;
import codedriver.framework.restful.dto.ApiVo;
@Service
@IsActived
public class ApiManageAuditListApi extends ApiComponentBase {

	@Autowired
	private ApiMapper ApiMapper;
	
	@Override
	public String getToken() {
		return "apimanage/audit/list";
	}

	@Override
	public String getName() {
		return "接口调用记录列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "token", type = ApiParamType.STRING, isRequired = true, desc = "接口token"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc="当前页码，默认值1"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc="页大小，默认值10")
	})
	@Output({
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc="当前页码"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "页大小"),
		@Param(name = "pageCount", type = ApiParamType.INTEGER, desc = "总页数"),
		@Param(name = "rowNum", type = ApiParamType.INTEGER, desc = "总行数"),
		@Param(name = "apiAuditList", explode = ApiAuditVo[].class, isRequired = true, desc = "接口调用记录列表")
	})
	@Description(desc = "接口调用记录列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ApiAuditVo apiAuditVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ApiAuditVo>() {});
		
		ApiVo api = ApiMapper.getApiByToken(apiAuditVo.getToken());
		if(api == null) {
			throw new ApiNotFoundException("token为'" + apiAuditVo.getToken() + "'的接口不存在");
		}
		
		int rowNum = ApiMapper.getApiAuditCount(apiAuditVo);
		int pageCount = PageUtil.getPageCount(rowNum, apiAuditVo.getPageSize());
		List<ApiAuditVo> apiAuditList = ApiMapper.getApiAuditList(apiAuditVo);
		
		JSONObject resultObj = new JSONObject();
		resultObj.put("apiAuditList", apiAuditList);
		resultObj.put("rowNum", rowNum);
		resultObj.put("pageCount", pageCount);
		resultObj.put("currentPage", apiAuditVo.getCurrentPage());
		resultObj.put("pageSize", apiAuditVo.getPageSize());
		return resultObj;
	}

}
