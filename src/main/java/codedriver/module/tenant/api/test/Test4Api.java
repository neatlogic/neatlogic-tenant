package codedriver.module.tenant.api.test;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
@Service
public class Test4Api extends PrivateApiComponentBase {
	
	@Override
	public String getToken() {
		return "test4";
	}

	@Override
	public String getName() {
		return "测试4";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return "test4";
	}

}
