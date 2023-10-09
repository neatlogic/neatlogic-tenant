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

package neatlogic.module.tenant.api.user;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.USER_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@AuthAction(action = USER_MODIFY.class)
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetUserTokenApi extends PrivateApiComponentBase {

    @Resource
    private UserService userService;
    @Resource
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "user/token/get";
    }

    @Override
    public String getName() {
        return "nmtau.getusertokenapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "userUuid", type = ApiParamType.STRING, desc = "common.useruuid", isRequired = true)})
    @Output({@Param(name = "Return", explode = String.class, desc = "nmra.savewebhookdataapi.input.param.desc")})
    @Description(desc = "nmtau.getusertokenapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String userUuid = jsonObj.getString("userUuid");
        if(userMapper.getUserBaseInfoByUuid(userUuid) == null) {
            throw new UserNotFoundException(userUuid);
        }
        return userService.getUserTokenByUser(userUuid);
    }
}
