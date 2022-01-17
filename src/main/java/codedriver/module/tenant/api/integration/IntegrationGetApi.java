/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.integration;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dependency.constvalue.FromType;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationGetApi extends PrivateApiComponentBase {

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
		int count = DependencyManager.getDependencyCount(FromType.INTEGRATION, integrationVo.getUuid());
		integrationVo.setReferenceCount(count);
		return integrationVo;
	}
}
