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

package neatlogic.module.tenant.api.constvalue;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.EnumFactory;
import neatlogic.framework.common.constvalue.IEnum;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.exception.constvalue.EnumNotFoundException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class UniversalEnumGetBatchApi extends PrivateApiComponentBase {
    @Override
    public String getToken() {
        return "universal/enum/get/batch";
    }

    @Override
    public String getName() {
        return "根据多个枚举类名获取枚举值";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "enumClassList", type = ApiParamType.JSONARRAY, desc = "枚举完整类名列表", isRequired = true)
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.JSONOBJECT)
    })
    @Description(desc = "根据多个枚举类名获取枚举值")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray enumClassArray = jsonObj.getJSONArray("enumClassList");
        if (CollectionUtils.isEmpty(enumClassArray)) {
            throw new ParamNotExistsException("enumClassList");
        }
        JSONObject resultObj = new JSONObject();
        List<String> enumClassList = enumClassArray.toJavaList(String.class);
        for (String enumClass : enumClassList) {
            Class<? extends IEnum> aClass = EnumFactory.getEnumClass(enumClass);
            if (aClass == null) {
                throw new EnumNotFoundException(enumClass);
            }
            Object instance = null;
            Object[] objects = aClass.getEnumConstants();
            if (objects != null && objects.length > 0) {
                instance = objects[0];
            } else {
                instance = aClass.newInstance();
            }
            resultObj.put(enumClass, aClass.getMethod("getValueTextList").invoke(instance));
        }
        return resultObj;
    }
}
