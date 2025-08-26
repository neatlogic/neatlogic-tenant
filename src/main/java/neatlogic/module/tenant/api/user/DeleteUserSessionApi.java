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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
