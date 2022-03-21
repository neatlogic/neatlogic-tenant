/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.integration;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dependency.constvalue.FrameworkFromType;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.exception.integration.IntegrationReferencedCannotBeDeletedException;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.auth.label.INTERFACE_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class IntegrationDeleteApi extends PrivateApiComponentBase {

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "integration/delete";
	}

	@Override
	public String getName() {
		return "集成设置删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "uuid", isRequired = true) })
	@Description(desc = "集成设置删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		if(DependencyManager.getDependencyCount(FrameworkFromType.INTEGRATION, uuid) > 0){
			throw new IntegrationReferencedCannotBeDeletedException(uuid);
		}
		integrationMapper.deleteIntegrationByUuid(uuid);
		return null;
	}
}
