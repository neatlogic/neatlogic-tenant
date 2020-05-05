package codedriver.module.tenant.api.integration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@IsActived
public class IntegrationSaveApi extends ApiComponentBase {

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "integration/save";
	}

	@Override
	public String getName() {
		return "集成配置保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "uuid，为空代表新增"), 
		@Param(name = "name", type = ApiParamType.STRING, desc = "名称", isRequired = true, xss = true),
		@Param(name = "url", type = ApiParamType.REGEX, desc = "目标地址", isRequired = true, rule = "^((http|ftp|https)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?"), @Param(name = "handler", type = ApiParamType.STRING, desc = "组件", isRequired = true, xss = true),
			@Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "配置，json格式", isRequired = true) })
	@Description(desc = "集成配置保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		IntegrationVo integrationVo = JSONObject.toJavaObject(jsonObj, IntegrationVo.class);
		if (StringUtils.isNotBlank(jsonObj.getString("uuid"))) {
			integrationMapper.updateIntegration(integrationVo);

		} else {
			integrationMapper.insertIntegration(integrationVo);
		}
		return null;
	}
}
