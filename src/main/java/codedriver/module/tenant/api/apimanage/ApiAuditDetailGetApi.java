package codedriver.module.tenant.api.apimanage;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.restful.dao.mapper.ApiMapper;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiAuditDetailGetApi extends ApiComponentBase {

	@Autowired
	private ApiMapper apiMapper;
	
	@Override
	public String getToken() {
		return "apimanage/audit/detail/get";
	}

	@Override
	public String getName() {
		return "获取接口调用记录接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "hash", type = ApiParamType.STRING, desc = "内容uuid", isRequired = true)})
	@Output({})
	@Description(desc = "获取接口调用记录接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return apiMapper.getApiAuditDetailByHash(jsonObj.getString("hash"));
	}

}
