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
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.USER_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.Md5Util;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

@AuthAction(action = USER_MODIFY.class)
@Service
@OperationType(type = OperationTypeEnum.OPERATE)
public class ResetUserTokenApi extends PrivateApiComponentBase {

    @Resource
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "user/token/reset";
    }

    @Override
    public String getName() {
        return "nmtau.resetusertokenapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "userUuid", type = ApiParamType.STRING, desc = "common.useruuid", isRequired = true)})
    @Output({@Param(name = "Return", explode = String.class, desc = "nmtau.resetcurrentusertokenapi.output.param.return.desc")})
    @Description(desc = "nmtau.resetusertokenapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String userUuid = jsonObj.getString("userUuid");
        if(userMapper.getUserBaseInfoByUuid(userUuid) == null) {
            throw new UserNotFoundException(userUuid);
        }
        String token = Md5Util.encryptMD5(UUID.randomUUID().toString());
        userMapper.updateUserTokenByUuid(token, userUuid);
        return token;
    }
}
