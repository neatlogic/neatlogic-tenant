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
