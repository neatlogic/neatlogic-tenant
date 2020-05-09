package codedriver.module.tenant.api.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.auth.core.AuthBase;
import codedriver.framework.auth.core.AuthFactory;
import codedriver.framework.common.util.ModuleUtil;
import codedriver.framework.dto.AuthGroupVo;
import codedriver.framework.dto.AuthVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.RoleService;
import codedriver.module.tenant.service.UserService;

@Service
public class AuthSearchApi extends ApiComponentBase {

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;

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
    	@Param( name = "groupName", type = ApiParamType.STRING, desc = "权限组名"),
        @Param( name = "keyword", type = ApiParamType.STRING, desc = "关键字")
    })
    @Output({
        @Param( name = "authGroupList", type = ApiParamType.JSONARRAY, desc = "权限列表组集合", explode = AuthGroupVo[].class)
    })
    @Description(desc = "权限列表查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        String groupName = jsonObj.getString("groupName");
        String keyword = jsonObj.getString("keyword");
        List<AuthGroupVo> authGroupVoList = new ArrayList<>();
        List<AuthVo> roleAuthList = roleService.getRoleCountByAuth();
        Map<String, Integer> roleAuthMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(roleAuthList)){
            for (AuthVo roleAuth : roleAuthList){
                roleAuthMap.put(roleAuth.getName(), roleAuth.getRoleCount());
            }
        }
        List<AuthVo> userAuthList = userService.getUserCountByAuth();
        Map<String, Integer> userAuthMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(userAuthList)){
            for (AuthVo userAuth : userAuthList){
                userAuthMap.put(userAuth.getName(), userAuth.getUserCount());
            }
        }

        Map<String, List<AuthBase>> authGroupMap = AuthFactory.getAuthGroupMap();
        for (Map.Entry<String, List<AuthBase>> entry : authGroupMap.entrySet()){
            String authGroupName = entry.getKey();
            boolean keyIsvalid = StringUtils.isNotBlank(keyword);
            if(!TenantContext.get().getActiveModuleMap().containsKey(authGroupName) || (groupName != null && !groupName.equalsIgnoreCase(authGroupName))) {
            	continue;
            }
            AuthGroupVo authGroupVo = new AuthGroupVo();
            authGroupVo.setName(authGroupName);
            authGroupVo.setDisplayName(ModuleUtil.getModuleGroup(authGroupName).getGroupName());
            List<AuthBase> authList = authGroupMap.get(authGroupName);
            if (authList != null && authList.size() > 0){
                List<AuthVo> authArray = new ArrayList<>();
                for (AuthBase authBase : authList){
                    if ( (!keyIsvalid) || (keyIsvalid && authBase.getAuthDisplayName().contains(keyword))){
                        AuthVo authVo = new AuthVo();
                        authVo.setName(authBase.getAuthName());
                        authVo.setDisplayName(authBase.getAuthDisplayName());
                        authVo.setDescription(authBase.getAuthIntroduction());
                        if (roleAuthMap.containsKey(authVo.getName())){
                            authVo.setRoleCount(roleAuthMap.get(authVo.getName()));
                        }
                        if (userAuthMap.containsKey(authVo.getName())){
                            authVo.setUserCount(userAuthMap.get(authVo.getName()));
                        }
                        authArray.add(authVo);
                    }

                }
                authGroupVo.setAuthVoList(authArray);
            }
            authGroupVoList.add(authGroupVo);
        }
        returnObj.put("authGroupList", authGroupVoList);
        return returnObj;
    }
}
