package codedriver.module.tenant.api.test;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.ApiComponentBase;

@Transactional
@Component
public class TestApi extends ApiComponentBase {

	@Autowired
	private UserMapper userMapper;

	@Override
	public String getToken() {
		return "/haha/test";
	}

	@Override
	public String getName() {
		return "测试输出log接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Description(desc = "测试输出log接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		UserVo userVo = new UserVo();
		userVo.setUuid("20dea39b97cf11ea94ff005056c00001");
		userVo.setIsActive(1);
		userMapper.updateUserActive(userVo);
		return null;
	}

}
