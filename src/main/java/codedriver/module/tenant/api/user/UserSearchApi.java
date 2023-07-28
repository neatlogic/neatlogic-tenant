/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.user;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.ROLE_MODIFY;
import codedriver.framework.auth.label.TEAM_MODIFY;
import codedriver.framework.auth.label.USER_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = TEAM_MODIFY.class)
@AuthAction(action = ROLE_MODIFY.class)
@AuthAction(action = USER_MODIFY.class)
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
        return "查询用户";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字(用户id或名称),模糊查询", xss = true),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "状态"),
            @Param(name = "vipLevel", type = ApiParamType.INTEGER, desc = "VIP等级"),
            @Param(name = "authGroup", type = ApiParamType.STRING, desc = "权限模块"),
            @Param(name = "auth", type = ApiParamType.STRING, desc = "权限"),
            @Param(name = "teamUuid", type = ApiParamType.STRING, desc = "用户组uuid"),
            @Param(name = "roleUuid", type = ApiParamType.STRING, desc = "角色uuid"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页展示数量 默认0"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页"),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "默认值列表")
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, explode = UserVo[].class, desc = "table数据列表"),
            @Param(name = "pageCount", type = ApiParamType.INTEGER, desc = "总页数"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页展示数量"),
            @Param(name = "rowNum", type = ApiParamType.INTEGER, desc = "总条目数")
    })
    @Description(desc = "查询用户接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {

        JSONObject resultObj = new JSONObject();
        UserVo userVo = JSON.toJavaObject(jsonObj, UserVo.class);
        JSONArray defaultValue = userVo.getDefaultValue();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            List<String> uuidList = defaultValue.toJavaList(String.class);
            List<UserVo> userList = userMapper.getUserByUserUuidList(uuidList);
            resultObj.put("tbodyList", userList);
            return resultObj;
        }
        userVo.setIsDelete(0);
        if (userVo.getNeedPage()) {
            int rowNum = userMapper.searchUserCount(userVo);
            resultObj.put("rowNum", rowNum);
            resultObj.put("pageSize", userVo.getPageSize());
            resultObj.put("currentPage", userVo.getCurrentPage());
            resultObj.put("pageCount", PageUtil.getPageCount(rowNum, userVo.getPageSize()));
        }
        resultObj.put("tbodyList", userMapper.searchUser(userVo));
        return resultObj;
    }
}
