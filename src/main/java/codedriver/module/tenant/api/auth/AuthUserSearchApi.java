/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.auth;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import codedriver.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: codedriver
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

    @Input( {
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字"),
            @Param( name = "auth",  desc = "权限", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页展示数量 默认10"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页")
    })

    @Output({
            @Param( name = "userList", desc = "用户列表", type = ApiParamType.JSONARRAY, explode = UserVo[].class),
    })

    @Description(desc = "权限用户查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        UserVo vo = JSONObject.toJavaObject(jsonObj, UserVo.class);
        List<UserVo> userList = new ArrayList<>();
        int rowNum = userMapper.searchUserCountByAuth(vo);
        if (vo != null) {
            List<String> uuidList = userMapper.searchUserUuIdByUser(vo);
            if (CollectionUtils.isNotEmpty(uuidList)) {
                userList = userMapper.getUserByUserUuidList(uuidList);
            }
        }
        vo.setRowNum(rowNum);
        return TableResultUtil.getResult(userList, vo);
    }
}
