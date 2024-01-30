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
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateRawApiComponentBase;

//@Component
//@Transactional
public class TestApi extends PrivateRawApiComponentBase {

    @Override
    public String getName() {
        return "测试RAW接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "测试RAW接口")
    @Output({@Param(name = "result", type = ApiParamType.STRING)})
    @Override
    public Object myDoService(String param) throws Exception {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("result", param);
        return jsonObj;
    }

    @Override
    public String getToken() {
        return "/testraw";
    }

}
