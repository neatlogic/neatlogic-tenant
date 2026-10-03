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

package neatlogic.module.tenant.api.user;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.USER_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.systemuser.ISystemUser;
import neatlogic.framework.common.constvalue.systemuser.SystemUserFactory;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

/**
 * 查询已注册系统内置用户的授权管理列表，仅暴露身份显示字段。
 */
@Service
@AuthAction(action = USER_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchSystemUserApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "user/system/search";
    }

    @Override
    public String getName() {
        return "nmtau.searchsystemuserapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    /**
     * 直接读取注册定义，名称在当前请求语言下生成，避免构造包含 JWT 的用户对象。
     */
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "common.tbodylist")
    })
    @Description(desc = "nmtau.searchsystemuserapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        JSONArray tbodyList = new JSONArray();
        for (ISystemUser systemUser : SystemUserFactory.getSystemUserList()) {
            JSONObject userObj = new JSONObject();
            userObj.put("uuid", systemUser.getUserUuid());
            userObj.put("userId", systemUser.getUserId());
            userObj.put("userName", systemUser.getUserName());
            tbodyList.add(userObj);
        }
        JSONObject result = new JSONObject();
        result.put("tbodyList", tbodyList);
        return result;
    }
}
