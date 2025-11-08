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

package neatlogic.module.tenant.api.auth;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
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
@AuthAction(action = NoAuth.class)
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
