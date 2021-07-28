/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.integration;

import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dto.IntegrationHandlerVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationHandlerListApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "integration/handler/list";
	}

	@Override
	public String getName() {
		return "集成信息处理组件列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Output({ @Param(name = "Return", explode = IntegrationHandlerVo[].class, desc = "信息处理组件列表") })
	@Description(desc = "集成信息处理组件列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return IntegrationHandlerFactory.getHandlerList();
	}
}
