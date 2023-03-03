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
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional

@OperationType(type = OperationTypeEnum.UPDATE)
public class CurrentUserSaveApi extends PrivateApiComponentBase {

    @Autowired
    UserMapper userMapper;

    @Autowired
    TeamMapper teamMapper;

    @Override
    public String getToken() {
        return "user/current/save";
    }

    @Override
    public String getName() {
        return "保存当前用户接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "userName", type = ApiParamType.STRING, desc = "用户姓名", isRequired = true, xss = true),
        @Param(name = "email", type = ApiParamType.STRING, desc = "用户邮箱", isRequired = false, xss = true),
        @Param(name = "phone", type = ApiParamType.STRING, desc = "用户电话", isRequired = false, xss = true),
        @Param(name = "userInfo", type = ApiParamType.STRING, desc = "其他信息", isRequired = false, xss = true)})
    @Output({})
    @Description(desc = "保存用户接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        UserVo currentUserVo = userMapper.getUserBaseInfoByUuid(UserContext.get().getUserUuid(true));
        currentUserVo.setUserName(jsonObj.getString("userName"));
        currentUserVo.setEmail(jsonObj.getString("email"));
        currentUserVo.setPhone(jsonObj.getString("phone"));
        currentUserVo.setUserInfo(jsonObj.getString("userInfo"));
        userMapper.updateUser(currentUserVo);
        return currentUserVo.getUuid();
    }
}
