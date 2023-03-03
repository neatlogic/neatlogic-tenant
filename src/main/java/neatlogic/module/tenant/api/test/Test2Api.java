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

import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Deprecated
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class Test2Api extends PublicApiComponentBase {

    @Override
    public String getName() {
        return "测试2";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "测试公共接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return "test2";
    }

}
