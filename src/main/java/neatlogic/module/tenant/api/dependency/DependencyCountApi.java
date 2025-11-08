/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.dependency;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
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
@AuthAction(action = NoAuth.class)
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
