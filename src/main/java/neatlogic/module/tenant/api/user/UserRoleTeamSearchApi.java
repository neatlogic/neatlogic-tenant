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
