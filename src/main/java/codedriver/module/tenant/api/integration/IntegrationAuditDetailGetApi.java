package codedriver.module.tenant.api.integration;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationAuditDetailGetApi extends ApiComponentBase {

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "integration/audit/detail/get";
	}

	@Override
	public String getName() {
		return "获取审计内容接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "hash", type = ApiParamType.STRING, desc = "内容uuid", isRequired = true) })
	@Output({ @Param(type = ApiParamType.STRING) })
	@Description(desc = "获取审计内容接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return integrationMapper.getIntegrationAuditDetailByHash(jsonObj.getString("hash"));
	}
}
