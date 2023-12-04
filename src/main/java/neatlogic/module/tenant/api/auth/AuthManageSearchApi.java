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

package neatlogic.module.tenant.api.auth;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthBase;
import neatlogic.framework.auth.core.AuthFactory;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.ModuleUtil;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.AuthVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class AuthManageSearchApi extends PrivateApiComponentBase {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "auth/manage/search";
    }

    @Override
    public String getName() {
        return "权限管理列表查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "groupName", type = ApiParamType.STRING, desc = "权限组名"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字")
    })
    @Output({
            @Param(type = ApiParamType.JSONARRAY, desc = "权限列表组集合", explode = AuthVo[].class)
    })
    @Description(desc = "权限管理列表查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<AuthVo> authVoList = new ArrayList<>();
        String groupName = jsonObj.getString("groupName");
        String keyword = jsonObj.getString("keyword");
        if ("all".equals(groupName)) {
            groupName = null;
        }
        List<AuthVo> roleAuthList = roleMapper.getRoleCountByAuth();
        Map<String, Integer> roleAuthMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(roleAuthList)) {
            for (AuthVo roleAuth : roleAuthList) {
                roleAuthMap.put(roleAuth.getName(), roleAuth.getRoleCount());
            }
        }
        List<AuthVo> userAuthList = userMapper.getUserCountByAuth();
        Map<String, Integer> userAuthMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(userAuthList)) {
            for (AuthVo userAuth : userAuthList) {
                userAuthMap.put(userAuth.getName(), userAuth.getUserCount());
            }
        }
        Map<String, List<AuthBase>> authGroupMap = AuthFactory.getAuthGroupMap();
        for (Map.Entry<String, List<AuthBase>> entry : authGroupMap.entrySet()) {
            String authGroupName = entry.getKey();
            if (!TenantContext.get().getActiveModuleMap().containsKey(authGroupName) || (groupName != null && !groupName.equalsIgnoreCase(authGroupName))) {
                continue;
            }
            String displayName = ModuleUtil.getModuleGroup(authGroupName).getGroupName();
            List<AuthBase> authList = authGroupMap.get(authGroupName);
            if (authList != null && authList.size() > 0) {
                List<AuthVo> authArray = new ArrayList<>();
                for (AuthBase authBase : authList) {
                    if (authBase.isShow() && (StringUtils.isBlank(keyword) || authBase.getAuthDisplayName().contains(keyword))) {
                        AuthVo authVo = new AuthVo(authBase.getAuthName(), authBase.getAuthDisplayName(), authBase.getAuthIntroduction(), displayName, authBase.getSort());
                        if (roleAuthMap.containsKey(authVo.getName())) {
                            authVo.setRoleCount(roleAuthMap.get(authVo.getName()));
                        }
                        if (userAuthMap.containsKey(authVo.getName())) {
                            authVo.setUserCount(userAuthMap.get(authVo.getName()));
                        }
                        authArray.add(authVo);
                    }
                }
                authArray.sort(Comparator.comparing(AuthVo::getSort));
                authVoList.addAll(authArray);
            }
        }
        return authVoList;
    }
}
