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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class UserSearchApi extends PrivateApiComponentBase {

    @Resource
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "user/search";
    }

    @Override
    public String getName() {
        return "nmtau.usersearchapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword", xss = true, help = "用户id或名称或email"),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "common.isactive"),
            @Param(name = "vipLevel", type = ApiParamType.INTEGER, desc = "VIP等级"),
            @Param(name = "teamUuid", type = ApiParamType.STRING, desc = "common.teamuuid"),
            @Param(name = "roleUuid", type = ApiParamType.STRING, desc = "common.roleuuid"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "common.defaultvalue")
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, explode = UserVo[].class, desc = "common.tbodylist"),
            @Param(explode = BasePageVo.class, desc = "common.pageinfo")
    })
    @Description(desc = "nmtau.usersearchapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        UserVo userVo = JSON.toJavaObject(jsonObj, UserVo.class);
        JSONArray defaultValue = userVo.getDefaultValue();
        List<UserVo> userList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            List<String> uuidList = defaultValue.toJavaList(String.class);
            userList = userMapper.getUserByUserUuidList(uuidList);
            return TableResultUtil.getResult(userList);
        }else {
            userVo.setIsDelete(0);
            int rowNum = userMapper.searchUserCount(userVo);
            userVo.setRowNum(rowNum);
            if(rowNum > 0){
                List<String> userUuidList = userMapper.searchUserUuidList(userVo);
                if (CollectionUtils.isNotEmpty(userUuidList)) {
                    userList = userMapper.searchUserDetailInfoByUuidList(userUuidList);
                }
            }
        }
        return TableResultUtil.getResult(userList, userVo);
    }
}
