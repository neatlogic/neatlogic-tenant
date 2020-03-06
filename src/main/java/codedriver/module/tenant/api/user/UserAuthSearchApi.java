package codedriver.module.tenant.api.user;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.RoleAuthVo;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.UserService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserAuthSearchApi extends ApiComponentBase {

    @Autowired
    private UserService userService;

    @Override
    public String getToken() {
        return "user/auth/search";
    }

    @Override
    public String getName() {
        return "用户权限查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param( name = "userId", type = ApiParamType.STRING, desc = "用户ID", isRequired = true)
    })
    @Output({
            @Param( name = "userAuthList", type = ApiParamType.JSONARRAY, desc = "用户权限集合"),
            @Param( name = "userRoleAuthList", type = ApiParamType.JSONARRAY, desc = "用户角色权限集合")
    })
    @Description(desc = "用户权限查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        String userId = jsonObj.getString("userId");
        List<UserAuthVo> userAuthList = userService.searchUserAuth(userId);
        List<RoleAuthVo> userRoleAuthList = userService.searchUserRoleAuth(userId);

        JSONArray userRoleAuthArray = new JSONArray();
        Map<String, String> userRoleAuthMap = new HashMap<>();

        if (userRoleAuthList != null && userRoleAuthList.size() > 0) {
            for (RoleAuthVo roleAuth : userRoleAuthList) {
                if (userRoleAuthMap.containsKey(roleAuth.getAuth())){
                    if (roleAuth.getAuthGroup().equals(userRoleAuthMap.get(roleAuth.getAuth()))){
                        continue;
                    }
                }
                userRoleAuthMap.put(roleAuth.getAuth(), roleAuth.getAuthGroup());
                JSONObject userRoleAuthObj = new JSONObject();
                userRoleAuthObj.put("auth", roleAuth.getAuth());
                userRoleAuthObj.put("authGroup", roleAuth.getAuthGroup());

                userRoleAuthArray.add(userRoleAuthObj);
            }
        }

        JSONArray userAuthArray = new JSONArray();
        if (userAuthList != null && userAuthList.size() > 0) {
            for (UserAuthVo authVo : userAuthList) {
                boolean sameAuth = userRoleAuthMap.containsKey(authVo.getAuth());
                boolean sameGroup = false;
                if (sameAuth){
                    sameGroup = userRoleAuthMap.get(authVo.getAuth()).equals(authVo.getAuthGroup());
                }
                if (!sameAuth || !sameGroup) {
                    JSONObject userAuthObj = new JSONObject();
                    userAuthObj.put("auth", authVo.getAuth());
                    userAuthObj.put("authGroup", authVo.getAuthGroup());
                    userAuthArray.add(userAuthObj);
                }
            }
        }
        returnObj.put("userAuthList", userAuthArray);
        returnObj.put("userRoleAuthList", userRoleAuthArray);
        return returnObj;
    }
}
