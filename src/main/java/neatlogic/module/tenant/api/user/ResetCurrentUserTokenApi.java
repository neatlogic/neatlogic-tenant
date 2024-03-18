/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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
public class ResetCurrentUserTokenApi extends PrivateApiComponentBase {

    @Resource
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "user/current/token/reset";
    }

    @Override
    public String getName() {
        return "nmtau.resetcurrentusertokenapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(name = "Return", explode = String.class, desc = "nmtau.resetcurrentusertokenapi.output.param.return.desc")})
    @Description(desc = "nmtau.resetcurrentusertokenapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String token = Md5Util.encryptMD5(UUID.randomUUID().toString());
        userMapper.updateUserTokenByUuid(token, UserContext.get().getUserUuid(true));
        return token;
    }
}
