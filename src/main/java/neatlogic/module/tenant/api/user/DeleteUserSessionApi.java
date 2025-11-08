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
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.UserSessionMapper;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class DeleteUserSessionApi extends PrivateApiComponentBase {
    @Resource
    UserSessionMapper userSessionMapper;

    @Override
    public String getToken() {
        return "/user/session/delete";
    }

    @Override
    public String getName() {
        return "nmtau.deleteusersessionapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "userUuidList", type = ApiParamType.JSONARRAY, desc = "nmtau.deleteusersessionapi.input.param.uuidlist")
    })
    @Output({})
    @Description(desc = "nmtau.deleteusersessionapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray userUuidArray = jsonObj.getJSONArray("userUuidList");
        if (CollectionUtils.isNotEmpty(userUuidArray)) {
            List<String> userUuidList = userUuidArray.toJavaList(String.class);
            userSessionMapper.deleteUserSessionByUserUuidList(userUuidList);
        }
        return null;
    }
}
