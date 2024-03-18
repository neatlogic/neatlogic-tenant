/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.test;

import neatlogic.framework.asynchronization.thread.NeatLogicThread;
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
            NeatLogicThread r = new NeatLogicThread("TESTS-SESSION") {
                @Override
                protected void execute() {
                    long s = System.currentTimeMillis();
                    for (int k = 0; k < 200; k++) {
                        userSessionMapper.getUserSessionByTokenHash(Integer.toString(k));
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
