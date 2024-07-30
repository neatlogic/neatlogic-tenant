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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserDataVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class GetUserDataApi extends PrivateApiComponentBase {

    @Autowired
    UserMapper userMapper;

    @Override
    public String getToken() {
        return "user/data/get";
    }

    @Override
    public String getName() {
        return "nmtau.getuserdataapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "type", type = ApiParamType.STRING, isRequired = true, desc = "common.type", help = "如果是用户默认模块数据，则应指定为defaultModulePage")
    })
    @Output({
            @Param(explode = UserDataVo.class)
    })
    @Description(desc = "nmtau.getuserdataapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String userUuid = UserContext.get().getUserUuid(true);
        String type = jsonObj.getString("type");
        return userMapper.getUserDataByUserUuidAndType(userUuid, type);
    }
}
