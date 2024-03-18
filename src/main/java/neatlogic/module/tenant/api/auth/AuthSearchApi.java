/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.auth;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthBase;
import neatlogic.framework.auth.core.AuthFactory;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.ModuleUtil;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.AuthGroupVo;
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
public class AuthSearchApi extends PrivateApiComponentBase {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "user/role/auth/search";
    }

    @Override
    public String getName() {
        return "用户角色管理权限列表查询接口";
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
            @Param(name = "authGroupList", type = ApiParamType.JSONARRAY, desc = "权限列表组集合", explode = AuthGroupVo[].class)
    })
    @Description(desc = "权限列表查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        String groupName = jsonObj.getString("groupName");
        String keyword = jsonObj.getString("keyword");
        List<AuthGroupVo> authGroupVoList = new ArrayList<>();
        // todo 除权限管理页外，无需计算角色数与用户数
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
            AuthGroupVo authGroupVo = new AuthGroupVo();
            authGroupVo.setName(authGroupName);
            authGroupVo.setDisplayName(ModuleUtil.getModuleGroup(authGroupName).getGroupName());
            List<AuthBase> authList = authGroupMap.get(authGroupName);
            if (authList != null && authList.size() > 0) {
                List<AuthVo> authArray = new ArrayList<>();
                for (AuthBase authBase : authList) {
                    if (StringUtils.isBlank(keyword) || authBase.getAuthDisplayName().contains(keyword)) {
                        AuthVo authVo = new AuthVo(authBase.getAuthName(), authBase.getAuthDisplayName(), authBase.getAuthIntroduction(), authBase.getSort());
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
                authGroupVo.setAuthVoList(authArray);
            }
            authGroupVoList.add(authGroupVo);
        }
        returnObj.put("authGroupList", authGroupVoList);
        return returnObj;
    }
}
