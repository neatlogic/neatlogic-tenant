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

package neatlogic.module.tenant.api.util;


import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.SubmitKeyManager;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class CleanSubmitKeyApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "/submit/key/clean";
    }

    @Override
    public String getName() {
        return "nmtau.cleansubmitkeyapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({
            @Param(name = "isAll", type = ApiParamType.STRING, desc = "1:清理所有 0:清理超时，默认清理超时")
    })
    @Output({})
    @Description(desc = "nmtau.cleansubmitkeyapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Integer isAll = paramObj.getInteger("isAll");
        if (isAll != null && isAll == 1) {
            SubmitKeyManager.clear();
        } else {
            SubmitKeyManager.cleanupExpiredKeys();
        }

        return null;
    }
}
