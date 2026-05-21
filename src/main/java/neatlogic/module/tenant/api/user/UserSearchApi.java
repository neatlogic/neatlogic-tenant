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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = NoAuth.class)
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
            @Param(name = "isOnline", type = ApiParamType.INTEGER, desc = "是否在线"),
            @Param(name = "teamUuid", type = ApiParamType.STRING, desc = "common.teamuuid"),
            @Param(name = "roleUuid", type = ApiParamType.STRING, desc = "common.roleuuid"),
            @Param(name = "teamUuidList", type = ApiParamType.JSONARRAY, desc = "common.teamuuidlist"),
            @Param(name = "roleUuidList", type = ApiParamType.JSONARRAY, desc = "common.roleuuidlist"),
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
