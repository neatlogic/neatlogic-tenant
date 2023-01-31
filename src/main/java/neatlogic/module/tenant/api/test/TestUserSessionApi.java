/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.test;

import neatlogic.framework.asynchronization.thread.CodeDriverThread;
import neatlogic.framework.asynchronization.threadpool.CachedThreadPool;
import neatlogic.framework.dao.mapper.UserSessionMapper;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;

import javax.annotation.Resource;

public class TestUserSessionApi extends PrivateApiComponentBase {

    @Resource
    private UserSessionMapper userSessionMapper;

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
            CodeDriverThread r = new CodeDriverThread("TESTS-SESSION") {
                @Override
                protected void execute() {
                    long s = System.currentTimeMillis();
                    for (int k = 0; k < 200; k++) {
                        userSessionMapper.getUserSessionByUserUuid(Integer.toString(k));
                        userSessionMapper.updateUserSession(Integer.toString(k));

                    }
                    System.out.println("耗时：" + (System.currentTimeMillis() - s) / 1000);
                }
            };
            CachedThreadPool.execute(r);
        }
        return null;
    }

}
