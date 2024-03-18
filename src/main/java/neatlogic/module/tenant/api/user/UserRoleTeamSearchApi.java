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

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.groupsearch.core.GroupSearchGroupVo;
import neatlogic.framework.restful.groupsearch.core.GroupSearchVo;
import neatlogic.framework.service.UserRoleTeamService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class UserRoleTeamSearchApi extends PrivateApiComponentBase {

    @Resource
    private UserRoleTeamService userRoleTeamService;

    @Override
    public String getToken() {
        return "user/role/team/search";
    }

    @Override
    public String getName() {
        return "nmmat.userroleteamsearchapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword", xss = true),
            @Param(name = "valueList", type = ApiParamType.JSONARRAY, desc = "common.defaultvaluelist"),
            @Param(name = "excludeList", type = ApiParamType.JSONARRAY, desc = "common.excludelist"),
            @Param(name = "includeList", type = ApiParamType.JSONARRAY, desc = "common.includelist", help = "‘当前登录人：common#loginuser’"),
            @Param(name = "groupList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "common.grouplist", help = "['processUserType','user','team','role']"),
            @Param(name = "rangeList", type = ApiParamType.JSONARRAY, desc = "common.rangelist", help = "['user#xxx','team#xxx','role#xxxx']"),
            @Param(name = "total", type = ApiParamType.INTEGER, desc = "common.rownum")
    })
    @Output({
            @Param(explode = GroupSearchGroupVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "nmmat.userroleteamsearchapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        GroupSearchVo groupSearchVo = jsonObj.toJavaObject(GroupSearchVo.class);
        return userRoleTeamService.searchUserRoleTeam(groupSearchVo);
    }

}
