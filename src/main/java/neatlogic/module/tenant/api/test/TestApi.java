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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.annotation.ResubmitInterval;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.transaction.core.AfterTransactionJob;
import neatlogic.module.tenant.dao.mapper.TestMapper;
import org.springframework.beans.factory.annotation.Autowired;

@Deprecated
//@Component
//@Transactional
public class TestApi extends PrivateApiComponentBase {

    @Autowired
    private TestMapper testMapper;

    @Override
    public String getName() {
        return "测试BG";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "测试BG")
    @ResubmitInterval(value = 10)
    @Input({@Param(name = "content", type = ApiParamType.STRING, isRequired = true)})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String content = jsonObj.getString("content");
        AfterTransactionJob<String> job = new AfterTransactionJob<>("TEST");
        job.execute(content, c -> {
            System.out.println(testMapper.getContent());
        });
        testMapper.insertContent(content);
        System.out.println("done");
        Thread.sleep(20000L);
        return null;
    }

    @Override
    public String getToken() {
        return "/testbg";
    }

}
