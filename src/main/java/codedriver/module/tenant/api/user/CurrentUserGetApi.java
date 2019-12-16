package codedriver.module.tenant.api.user;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class CurrentUserGetApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "/user/current/get";
	}

	@Override
	public String getName() {
		return "获取当前用户test接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({})
	@Output({})
	@Description(desc = "获取当前用户test接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return UserContext.get();
	}
}
