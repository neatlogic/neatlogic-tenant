package codedriver.module.tenant.api.integration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.integration.dto.PatternVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@IsActived
public class IntegrationHelpApi extends ApiComponentBase {

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "integration/help";
	}

	@Override
	public String getName() {
		return "获取集成设置参数说明接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "集成配置uuid", isRequired = true) })
	@Output({ @Param(explode = PatternVo[].class) })
	@Description(desc = "获取集成设置参数说明接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(jsonObj.getString("uuid"));
		JSONObject configObj = integrationVo.getConfig();
		IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
		if (handler == null) {
			throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
		}

		if (handler.hasPattern().equals(0)) {
			List<PatternVo> patternList = new ArrayList<>();
			if (configObj.getJSONObject("param") != null && configObj.getJSONObject("param").getJSONArray("paramList") != null) {
				for (int i = 0; i < configObj.getJSONObject("param").getJSONArray("paramList").size(); i++) {
					JSONObject paramObj = configObj.getJSONObject("param").getJSONArray("paramList").getJSONObject(i);
					PatternVo patternVo = JSONObject.toJavaObject(paramObj, PatternVo.class);
					patternList.add(patternVo);
				}
			}
			return patternList;
		} else {
			return handler.getInputPattern();
		}
	}
}
