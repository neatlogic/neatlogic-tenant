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

package neatlogic.module.tenant.api.dependency;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.dependency.core.FromTypeFactory;
import neatlogic.framework.dependency.core.IFromType;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class DependencyCountApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "dependency/count";
    }

    @Override
    public String getName() {
        return "查询引用数量";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, isRequired = true, minSize = 1, desc = "被调用者唯一标识是字符串类型的时候，通过uuid参数传入", help = "被调用者唯一标识是数字类型的时候，通过id参数传入；被调用者唯一标识是数字类型的时候，通过id参数传入"),
            @Param(name = "calleeType", type = ApiParamType.STRING, isRequired = true, desc = "被调用者类型")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.JSONOBJECT, desc = "引用数量")
    })
    @Description(desc = "查询引用数量")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        IFromType calleeType = FromTypeFactory.getCalleeType(paramObj.getString("calleeType"));
        if (calleeType == null) {
            throw new ParamIrregularException("calleeType（被调用者类型）", FromTypeFactory.getAllCalleeTypeToString());
        }
        JSONArray defaultValue = paramObj.getJSONArray("defaultValue");
        return DependencyManager.getBatchDependencyCount(calleeType, defaultValue);
    }
}
