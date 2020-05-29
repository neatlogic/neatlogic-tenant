package codedriver.module.tenant.api.test;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.ApiComponentBase;

@Component
public class MirrorApi extends ApiComponentBase {

	@Override
	public String getToken() {
		// TODO Auto-generated method stub
		return "test/mirror";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "反射测试api";
	}

	@Override
	public String getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRaw() {
		return true;
	}

	@Override
	@Description(desc = "反射测试api")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return jsonObj;
	}

}
