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
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.Md5Util;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

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
        return "重置自己的令牌";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(name = "Return", explode = String.class, desc = "新的令牌")})
    @Description(desc = "重置自己的令牌接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String token = Md5Util.encryptMD5(UUID.randomUUID().toString());
        userMapper.updateUserTokenByUuid(token, UserContext.get().getUserUuid(true));
        return token;
    }
}
