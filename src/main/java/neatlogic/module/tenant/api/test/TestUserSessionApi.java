/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
