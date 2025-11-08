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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthBase;
import neatlogic.framework.auth.core.AuthCSBase;
import neatlogic.framework.auth.core.AuthFactory;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.ModuleUtil;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.AuthVo;
import neatlogic.framework.dto.module.ModuleGroupVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@AuthAction(action = NoAuth.class)
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
        return "nmtaa.authmanagesearchapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "groupName", type = ApiParamType.STRING, desc = "nmtaa.authmanagesearchapi.input.param.desc.groupname"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "nmtaa.authmanagesearchapi.input.param.desc.defaultvalue"),
    })
    @Output({
            @Param(type = ApiParamType.JSONARRAY, desc = "权限列表组集合", explode = AuthVo[].class)
    })
    @Description(desc = "nmtaa.authmanagesearchapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<AuthVo> authVoList = new ArrayList<>();
        String groupName = jsonObj.getString("groupName");
        String keyword = jsonObj.getString("keyword");
        JSONArray defaultValue = jsonObj.getJSONArray("defaultValue");
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
            ModuleGroupVo moduleGroupVo = ModuleUtil.getModuleGroup(authGroupName);
            List<AuthBase> authList = authGroupMap.get(authGroupName);
            if (CollectionUtils.isNotEmpty(authList)) {
                List<AuthVo> authArray = new ArrayList<>();
                for (AuthBase authBase : authList) {
                    if (authBase.isShow() && ((StringUtils.isBlank(keyword) && CollectionUtils.isEmpty(defaultValue))
                            || (StringUtils.isNotBlank(keyword) && authBase.getAuthDisplayName().contains(keyword))
                            || (StringUtils.isNotBlank(keyword) && authBase.getAuthName().contains(keyword)))
                            || (CollectionUtils.isNotEmpty(defaultValue) && defaultValue.contains(authBase.getAuthName()))
                    ) {
                        AuthVo authVo = new AuthVo(authBase.getAuthName(), authBase.getAuthDisplayName(), authBase.getAuthIntroduction(), moduleGroupVo, authBase.getSort());
                        if (roleAuthMap.containsKey(authVo.getName())) {
                            authVo.setRoleCount(roleAuthMap.get(authVo.getName()));
                        }
                        if (userAuthMap.containsKey(authVo.getName())) {
                            authVo.setUserCount(userAuthMap.get(authVo.getName()));
                        }
                        if (authBase instanceof AuthCSBase) {
                            authVo.setCommercial(true);
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
