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
