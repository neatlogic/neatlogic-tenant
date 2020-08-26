package codedriver.module.tenant.api.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.dao.mapper.TestMapper;

@Transactional
public class TestUpdateSqlApi extends PrivateApiComponentBase {

	@Autowired
	private TestMapper testMapper;

	@Override
	public String getToken() {
		return "test/updatesql";
	}

	@Override
	public String getName() {
		return "测试更新sql";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Description(desc = "测试更新sql")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		testMapper.updateFormAttribute();
		return "OK";
	}

}
