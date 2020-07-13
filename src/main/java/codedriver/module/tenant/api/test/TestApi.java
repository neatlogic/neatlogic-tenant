package codedriver.module.tenant.api.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.ApiComponentBase;

@Component
public class TestApi extends ApiComponentBase {

	@Autowired
	private UserMapper userMapper;

	@Override
	public String getToken() {
		return "/test/loadclass";
	}

	@Override
	public String getName() {
		return "测试动态加载类";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Description(desc = "测试动态加载类")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return null;
	}

}
