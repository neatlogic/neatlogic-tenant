package codedriver.module.tenant.api.test;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.restful.core.ApiComponentBase;
public class Test3Api extends ApiComponentBase {
	
	@Override
	public boolean isPrivate() {
		return false;
	}

	@Override
	public String getToken() {
		return "test3";
	}

	@Override
	public String getName() {
		return "测试3";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return "test3";
	}

}
