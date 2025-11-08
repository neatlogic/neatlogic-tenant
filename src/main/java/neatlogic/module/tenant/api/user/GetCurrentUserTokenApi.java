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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetCurrentUserTokenApi extends PrivateApiComponentBase {

    @Resource
    private UserService userService;

    @Override
    public String getToken() {
        return "user/current/token/get";
    }

    @Override
    public String getName() {
        return "nmtau.getcurrentusertokenapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(name = "Return", explode = String.class, desc = "nmra.savewebhookdataapi.input.param.desc")})
    @Description(desc = "nmtau.getcurrentusertokenapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return userService.getUserTokenByUser(UserContext.get().getUserUuid(true));
    }
}
