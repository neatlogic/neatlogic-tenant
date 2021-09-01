/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.user;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.service.UserRoleTeamService;
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
