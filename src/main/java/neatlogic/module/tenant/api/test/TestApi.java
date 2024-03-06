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
import neatlogic.framework.asynchronization.threadlocal.RequestContext;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

//@Component
//@Transactional
public class TestApi extends PrivateApiComponentBase {

    @Override
    public String getName() {
        return "测试RAW接口";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Override
    public String getToken() {
        return "/testraw";
    }

    @Description(desc = "测试RAW接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        System.out.println(RequestContext.get().getRequest().getMethod());
        return paramObj;
    }
}
