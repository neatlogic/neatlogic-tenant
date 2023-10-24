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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import java.util.stream.Collectors;

@Service

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
            @Param(name = "teamUuid", type = ApiParamType.STRING, desc = "common.teamuuid"),
            @Param(name = "roleUuid", type = ApiParamType.STRING, desc = "common.roleuuid"),
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

        JSONObject resultObj = new JSONObject();
        UserVo userVo = JSON.toJavaObject(jsonObj, UserVo.class);
        JSONArray defaultValue = userVo.getDefaultValue();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            List<String> uuidList = defaultValue.toJavaList(String.class);
            List<UserVo> userList = userMapper.getUserByUserUuidList(uuidList);
            return TableResultUtil.getResult(userList);
        }
        userVo.setIsDelete(0);
        int rowNum = userMapper.searchUserCount(userVo);
        if (rowNum == 0) {
            return TableResultUtil.getResult(new ArrayList(), userVo);
        }
        userVo.setRowNum(rowNum);
        List<UserVo> userBaseInfoList = userMapper.searchUserBaseInfo(userVo);
        if (CollectionUtils.isEmpty(userBaseInfoList)) {
            return TableResultUtil.getResult(new ArrayList(), userVo);
        }
        List<String> uuidList = userBaseInfoList.stream().map(UserVo::getUuid).collect(Collectors.toList());
        List<UserVo> tbodyList = userMapper.searchUserDetailInfoByUuidList(uuidList);
        return TableResultUtil.getResult(tbodyList, userVo);
    }
}
