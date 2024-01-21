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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.auth.core.AuthBase;
import neatlogic.framework.auth.core.AuthFactory;
import neatlogic.framework.auth.init.MaintenanceMode;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.ModuleUtil;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.*;
import neatlogic.framework.dto.module.ModuleGroupVo;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-06-04
 **/
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class AuthModuleGetApi extends PrivateApiComponentBase {
    @Resource
    UserMapper userMapper;

    @Override
    public String getToken() {
        return "auth/module/get";
    }

    @Override
    public String getName() {
        return "获取用户模块对应权限";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({

    })

    @Output({
            @Param(explode = ModuleGroupVo.class),
            @Param(name = "authList[].authDisplayName", desc = "权限名", type = ApiParamType.STRING),
            @Param(name = "authList[].authGroup", desc = "模块分组", type = ApiParamType.STRING),
            @Param(name = "authList[].authIntroduction", desc = "权限介绍", type = ApiParamType.STRING),
            @Param(name = "authList[].authName", desc = "权限", type = ApiParamType.STRING),
    })

    @Description(desc = "根据用户获取模块以及对应的权限列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray returnArray = new JSONArray();
        Set<String> authSet = new HashSet<String>();
        //获取用户权限
        List<UserAuthVo> userAuthList = null;
        String userUuid = UserContext.get().getUserUuid(true);
        UserVo userVo = null;
        if (Config.ENABLE_MAINTENANCE() && Config.MAINTENANCE().equals(UserContext.get().getUserId())) {
            userVo = new UserVo();
            userVo.setUserId(Config.MAINTENANCE());
        } else {
            userVo = userMapper.getUserBaseInfoByUuid(userUuid);
            if (userVo == null) {
                throw new UserNotFoundException(userUuid);
            }
        }
        //超级管理员拥有所有权限
        if (userVo.getIsSuperAdmin() != null && userVo.getIsSuperAdmin()) {
            List<AuthBase> authBaseList = AuthFactory.getAuthList();
            List<UserAuthVo> userAuthVos = new ArrayList<>();
            for (AuthBase authBase : authBaseList) {
                userAuthVos.add(new UserAuthVo(userUuid, authBase));
            }
            userAuthList = userAuthVos;
        } else {
            //维护模式下 获取厂商维护人员信息
            if (Config.ENABLE_MAINTENANCE() && Config.MAINTENANCE().equals(UserContext.get().getUserId())) {
                userAuthList = MaintenanceMode.getMaintenanceUser().getUserAuthList();
            } else {
                AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
                userAuthList = userMapper.searchUserAllAuthByUserAuth(authenticationInfoVo);
                AuthActionChecker.getAuthList(userAuthList);
            }
        }
        for (UserAuthVo userAuth : userAuthList) {
            authSet.add(userAuth.getAuth());
        }
        //****获取用户默认模块首页开始****
        HashMap<String, Map<String, Object>> OuterMap = new HashMap<>(16);
        UserDataVo userDataVo = userMapper.getUserDataByUserUuidAndType(UserContext.get().getUserUuid(), "defaultModulePage");
        if (userDataVo != null) {
            String data = userDataVo.getData();
            JSONObject dataJson = JSONObject.parseObject(data);
            JSONArray defaultModulePageList = dataJson.getJSONArray("defaultModulePageList");
            for (int i = 0; i < defaultModulePageList.size(); i++) {
                JSONObject o = defaultModulePageList.getJSONObject(i);
                String group = o.getString("group");
                Integer isDefault = o.getInteger("isDefault");
                String defaultPage = o.getString("defaultPage");
                Map<String, Object> innerMap = new HashMap<>(2);
                innerMap.put("isDefault", isDefault);
                innerMap.put("defaultPage", defaultPage);
                OuterMap.put(group, innerMap);
            }
        }
        //****获取用户默认模块首页结束****
        Map<String, List<AuthBase>> authModuleMap = AuthFactory.getAuthGroupMap();
        List<ModuleGroupVo> activeModuleGroupList = TenantContext.get().getActiveModuleGroupList();
        for (ModuleGroupVo moduleGroupVo : activeModuleGroupList) {
            JSONObject moduleGroupJson = new JSONObject();
            //把用户默认模块首页配置放入moduleGroupJson中
            Map<String, Object> map = OuterMap.get(moduleGroupVo.getGroup());
            if (map != null) {
                moduleGroupJson.put("isDefault", map.get("isDefault"));
                moduleGroupJson.put("defaultPage", map.get("defaultPage"));
            } else {
                moduleGroupJson.put("isDefault", 0);
                moduleGroupJson.put("defaultPage", "");
            }
            moduleGroupJson.put("group", moduleGroupVo.getGroup());
            moduleGroupJson.put("groupName", moduleGroupVo.getGroupName());
            moduleGroupJson.put("groupSort", moduleGroupVo.getGroupSort());
            moduleGroupJson.put("description", ModuleUtil.getModuleGroup(moduleGroupVo.getGroup()).getGroupDescription());
            returnArray.add(moduleGroupJson);

            List<AuthVo> authBaseList = new ArrayList<AuthVo>();
            if (authModuleMap.containsKey(moduleGroupVo.getGroup())) {
                for (AuthBase tmpAuth : authModuleMap.get(moduleGroupVo.getGroup())) {
                    if (authSet.contains(tmpAuth.getAuthName()) && tmpAuth.isShow()) {
                        authBaseList.add(new AuthVo(tmpAuth));
                    }
                }
            }
            moduleGroupJson.put("authList", authBaseList);
        }
        returnArray.sort(Comparator.comparing(obj -> ((JSONObject) obj).getInteger("groupSort")));
        return returnArray;
    }
}
