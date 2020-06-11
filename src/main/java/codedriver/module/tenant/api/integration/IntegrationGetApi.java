package codedriver.module.tenant.api.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
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
public class IntegrationGetApi extends ApiComponentBase {

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "integration/get";
	}

	@Override
	public String getName() {
		return "获取集成设置信息接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "集成配置uuid", isRequired = true) })
	@Output({ @Param(explode = IntegrationVo.class) })
	@Description(desc = "获取集成设置信息接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(jsonObj.getString("uuid"));
		return integrationVo;
	}
}
