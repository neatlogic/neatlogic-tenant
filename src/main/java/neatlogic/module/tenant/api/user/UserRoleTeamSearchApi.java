/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.user;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.groupsearch.core.GroupSearchGroupVo;
import neatlogic.framework.restful.groupsearch.core.GroupSearchVo;
import neatlogic.framework.service.UserRoleTeamService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = NoAuth.class)
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
