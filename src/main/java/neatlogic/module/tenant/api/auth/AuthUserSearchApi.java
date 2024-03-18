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

package neatlogic.module.tenant.api.auth;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import neatlogic.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-03-13 12:01
 **/
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class AuthUserSearchApi extends PrivateApiComponentBase {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "auth/user/search";
    }

    @Override
    public String getName() {
        return "权限用户查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字"),
            @Param(name = "auth", desc = "权限", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页展示数量 默认10"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页")
    })

    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "用户列表"),
            @Param(explode = BasePageVo.class),
    })

    @Description(desc = "权限用户查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        UserVo vo = JSONObject.toJavaObject(jsonObj, UserVo.class);
        List<UserVo> userList = new ArrayList<>();
        int rowNum = userMapper.searchUserCountByAuth(vo);
        if (rowNum > 0) {
            List<String> uuidList = userMapper.searchUserUuIdByUser(vo);
            userList = userMapper.getUserByUserUuidList(uuidList);
        }
        vo.setRowNum(rowNum);
        return TableResultUtil.getResult(userList, vo);
    }
}
