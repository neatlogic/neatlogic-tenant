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
        return "用户角色及组织架构查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字(用户id或名称),模糊查询", isRequired = false, xss = true),
            @Param(name = "valueList", type = ApiParamType.JSONARRAY, desc = "用于回显的参数列表"),
            @Param(name = "excludeList", type = ApiParamType.JSONARRAY, desc = "用于过滤回显参数"),
            @Param(name = "includeList", type = ApiParamType.JSONARRAY, desc = "用于需要回显参数，‘当前登录人：common#loginuser’"),
            @Param(name = "groupList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "限制接口返回类型，['processUserType','user','team','role']"),
            @Param(name = "rangeList", type = ApiParamType.JSONARRAY, desc = "限制接口option范围，['user#xxx','team#xxx','role#xxxx']"),
            @Param(name = "total", type = ApiParamType.INTEGER, desc = "共展示数量 默认18", isRequired = false)
    })
    @Output({
            @Param(name = "text", type = ApiParamType.STRING, desc = "类型中文名"),
            @Param(name = "value", type = ApiParamType.STRING, desc = "类型"),
            @Param(name = "dataList[0].text", type = ApiParamType.STRING, desc = "类型具体选项名"),
            @Param(name = "dataList[0].value", type = ApiParamType.STRING, desc = "类型具体选项值")
    })
    @Description(desc = "用户角色及组织架构查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return userRoleTeamService.searchUserRoleTeam(jsonObj);
    }

}
