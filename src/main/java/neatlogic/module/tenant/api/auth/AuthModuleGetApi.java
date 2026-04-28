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
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.auth.core.AuthBase;
import neatlogic.framework.auth.core.AuthFactory;
import neatlogic.framework.auth.init.MaintenanceMode;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.ModuleUtil;
import neatlogic.framework.config.ConfigManager;
import neatlogic.framework.config.FrameworkTenantConfig;
import neatlogic.framework.dao.mapper.HomePageMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.*;
import neatlogic.framework.dto.module.ModuleManageSettingVo;
import neatlogic.framework.dto.module.ModuleGroupVo;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dao.mapper.ModuleManageSettingMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@NoPasswordExpiredCheck
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class AuthModuleGetApi extends PrivateApiComponentBase {
    @Resource
    UserMapper userMapper;

    @Resource
    HomePageMapper homePageMapper;

    @Resource
    private ModuleManageSettingMapper moduleManageSettingMapper;

    @Override
    public String getToken() {
        return "auth/module/get";
    }

    @Override
    public String getName() {
        return "nmtaa.authmodulegetapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({

    })

    @Output({
            @Param(explode = ModuleGroupVo.class),
            @Param(name = "authList[].authDisplayName", desc = "nmtaa.authmodulegetapi.output.param.desc.authdisplayname", type = ApiParamType.STRING),
            @Param(name = "authList[].authGroup", desc = "common.module.group", type = ApiParamType.STRING),
            @Param(name = "authList[].authIntroduction", desc = "nmtaa.authmodulegetapi.output.param.desc.authintroduction", type = ApiParamType.STRING),
            @Param(name = "authList[].authName", desc = "nfdc.deployappconfigaction.auth", type = ApiParamType.STRING),
    })

    @Description(desc = "nmtaa.authmodulegetapi.description.desc")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray returnArray = new JSONArray();
        Set<String> authSet = new HashSet<>();
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
        if (userDataVo == null) {
            List<Long> homePageIdList = homePageMapper.getHomePageIdListByAuthority(UserContext.get().getAuthenticationInfoVo());
            if (CollectionUtils.isNotEmpty(homePageIdList)) {
                HomePageVo homePage = homePageMapper.getMinSortHomePageByIdList(homePageIdList);
                if (homePage != null) {
                    userDataVo = new UserDataVo(UserContext.get().getUserUuid(), homePage.getConfig(), "defaultModulePage");
                }
            }
        }
        if (userDataVo != null) {
            JSONObject dataJson = userDataVo.getData();
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
        Map<String, JSONObject> moduleManageSettingMap = new HashMap<>(16);
        ModuleManageSettingVo moduleManageSettingVo = moduleManageSettingMapper.getModuleManageSetting();
        if (moduleManageSettingVo != null && moduleManageSettingVo.getConfig() != null) {
            JSONArray groupList = moduleManageSettingVo.getConfig().getJSONArray("groupList");
            if (CollectionUtils.isNotEmpty(groupList)) {
                for (int i = 0; i < groupList.size(); i++) {
                    JSONObject groupSetting = groupList.getJSONObject(i);
                    String group = groupSetting.getString("group");
                    if (StringUtils.isNotBlank(group)) {
                        moduleManageSettingMap.put(group, groupSetting);
                    }
                }
            }
        }
        for (ModuleGroupVo moduleGroupVo : activeModuleGroupList) {
            String disabledModuleGroupListStr = ConfigManager.getConfig(FrameworkTenantConfig.DISABLED_MODULEGROUPLIST);
            if(StringUtils.isNotBlank(disabledModuleGroupListStr)){
                List<String> disabledModuleGroupList = Arrays.asList(disabledModuleGroupListStr.split(","));
                if (CollectionUtils.isNotEmpty(disabledModuleGroupList) && disabledModuleGroupList.contains(moduleGroupVo.getGroup())){
                    continue;
                }
            }
            JSONObject moduleManageSetting = moduleManageSettingMap.get(moduleGroupVo.getGroup());
            String alias = moduleManageSetting == null ? null : moduleManageSetting.getString("alias");
            Integer groupSort = moduleManageSetting == null ? null : moduleManageSetting.getInteger("sort");
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
            moduleGroupJson.put("groupName", StringUtils.isNotBlank(alias) ? alias : moduleGroupVo.getGroupName());
            moduleGroupJson.put("alias", alias);
            moduleGroupJson.put("groupSort", groupSort == null ? moduleGroupVo.getGroupSort() + 100000 : groupSort);
            moduleGroupJson.put("description", ModuleUtil.getModuleGroup(moduleGroupVo.getGroup()).getGroupDescription());
            returnArray.add(moduleGroupJson);

            List<AuthVo> authBaseList = new ArrayList<>();
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
