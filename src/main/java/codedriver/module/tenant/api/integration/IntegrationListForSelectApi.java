package codedriver.module.tenant.api.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
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
public class IntegrationListForSelectApi extends ApiComponentBase {

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "integration/list/forselect";
	}

	@Override
	public String getName() {
		return "集成设置数据列表接口（搜索下拉框专用）";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({ 
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字"), 
        @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数量") 
	})
	@Output({  
		@Param(name = "integrationList", explode = IntegrationVo[].class, desc = "集成设置列表") 
	})
	@Description(desc = "集成设置数据列表接口（搜索下拉框专用）")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		IntegrationVo integrationVo = JSONObject.toJavaObject(jsonObj, IntegrationVo.class);
		List<ValueTextVo> valueTextList = new ArrayList<>();
		List<IntegrationVo> integrationList = integrationMapper.searchIntegration(integrationVo);
		for(IntegrationVo integration : integrationList) {
			if(Objects.equals(integration.getIsActive(), 1)) {
				valueTextList.add(new ValueTextVo(integration.getUuid(), integration.getName()));
			}
		}
		return valueTextList;
	}

}
