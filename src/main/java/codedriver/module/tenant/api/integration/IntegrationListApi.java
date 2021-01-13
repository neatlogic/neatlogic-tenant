package codedriver.module.tenant.api.integration;

import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationVo;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationListApi extends PrivateApiComponentBase {

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "integration/list";
	}

	@Override
	public String getName() {
		return "集成设置数据列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "handler", type = ApiParamType.STRING, desc = "组件")
	})
	@Output({ @Param(explode = IntegrationVo[].class, desc = "集成设置列表") })
	@Description(desc = "集成设置数据列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		IntegrationVo integrationVo = new IntegrationVo();
		integrationVo.setNeedPage(false);
		integrationVo.setIsActive(null);
		integrationVo.setHandler(jsonObj.getString("handler"));
		return integrationMapper.searchIntegration(integrationVo);
	}

}
