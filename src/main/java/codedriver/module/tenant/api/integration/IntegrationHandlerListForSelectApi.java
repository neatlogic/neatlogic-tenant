package codedriver.module.tenant.api.integration;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FRAMEWORK_BASE;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dto.IntegrationHandlerVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = FRAMEWORK_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationHandlerListForSelectApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "integration/handler/list/forselect";
	}

	@Override
	public String getName() {
		return "获取集成信息处理组件列表_下拉框";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Output({ @Param(explode = ValueTextVo[].class, desc = "信息处理组件列表") })
	@Description(desc = "获取集成信息处理组件列表_下拉框")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {

		List<ValueTextVo> resultList = new ArrayList<>();
		List<IntegrationHandlerVo> integrationHandlerVoList = IntegrationHandlerFactory.getHandlerList();
		if(CollectionUtils.isNotEmpty(integrationHandlerVoList)){
			for(IntegrationHandlerVo vo : integrationHandlerVoList){
				resultList.add(new ValueTextVo(vo.getHandler(),vo.getName()));
			}
		}

		return resultList;
	}
}
