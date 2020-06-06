package codedriver.module.tenant.api.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.asynchronization.threadpool.CachedThreadPool;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.restful.core.ApiComponentBase;

public class TestUserSessionApi extends ApiComponentBase {

	@Autowired
	private UserMapper userMapper;

	@Override
	public boolean isPrivate() {
		return false;
	}

	@Override
	public String getToken() {
		return "testsession";
	}

	@Override
	public String getName() {
		return "测试用户会话性能";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		for (int i = 0; i < 50; i++) {
			CodeDriverThread r = new CodeDriverThread() {
				@Override
				protected void execute() {
					long s = System.currentTimeMillis();
					for (int k = 0; k < 200; k++) {
						userMapper.getUserSessionByUserUuid(Integer.toString(k));
						userMapper.updateUserSession(Integer.toString(k));

					}
					System.out.println("耗时：" + (System.currentTimeMillis() - s) / 1000);
				}
			};
			CachedThreadPool.execute(r);
		}
		return null;
	}

}
