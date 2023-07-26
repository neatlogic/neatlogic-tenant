/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.user;

import java.util.ArrayList;
import java.util.List;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class UserSearchForSelectApi extends PrivateApiComponentBase {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "user/search/forselect";
    }

    @Override
    public String getName() {
        return "查询用户_下拉";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字(用户id或名称),模糊查询", xss = true),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "状态"),
            @Param(name = "teamUuid", type = ApiParamType.STRING, desc = "用户组uuid"),
            @Param(name = "roleUuid", type = ApiParamType.STRING, desc = "角色uuid"),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "用于回显的参数列表", xss = true),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数", isRequired = false),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页展示数量 默认10", isRequired = false)
    })
    @Output({
            @Param(name = "list", type = ApiParamType.JSONARRAY, desc = "选项列表"),
            @Param(name = "pageCount", type = ApiParamType.INTEGER, desc = "总页数"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页展示数量"),
            @Param(name = "rowNum", type = ApiParamType.INTEGER, desc = "总条目数")})
    @Description(desc = "查询用户_下拉")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        UserVo searchVo = jsonObj.toJavaObject(UserVo.class);
        List<UserVo> tbodyList = new ArrayList<>();
        List<UserVo> userList = new ArrayList<>();
        JSONArray defaultValue = searchVo.getDefaultValue();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            List<String> uuidList = defaultValue.toJavaList(String.class);
            userList = userMapper.getUserListByUuidList(uuidList);
        } else {
            int rowNum = userMapper.searchUserCount(searchVo);
            if (rowNum > 0) {
                searchVo.setRowNum(rowNum);
                userList = userMapper.searchUser(searchVo);
            }
        }
        if (CollectionUtils.isNotEmpty(userList)) {
            for (UserVo user : userList) {
                tbodyList.add(new UserVo(user.getUuid(), user.getUserId(), user.getUserName()));
            }
        }
        return TableResultUtil.getResult(tbodyList, searchVo);
    }
}
