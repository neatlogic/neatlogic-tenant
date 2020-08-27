package codedriver.module.tenant.api.test;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.restful.core.publicapi.PublicApiComponentBase;
@Service
public class Test2Api extends PublicApiComponentBase {
	
	@Override
	public String getName() {
		return "测试2";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return "test2";
	}

}
