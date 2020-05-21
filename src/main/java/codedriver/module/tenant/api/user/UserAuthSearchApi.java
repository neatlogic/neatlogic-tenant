package codedriver.module.tenant.api.user;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.RoleAuthVo;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
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
    private UserMapper userMapper;

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
            @Param( name = "userUuid", type = ApiParamType.STRING, desc = "用户uuid", isRequired = true)
    })
    @Output({
            @Param( name = "userAuthObj", type = ApiParamType.JSONARRAY, desc = "用户权限集合"),
            @Param( name = "userRoleAuthObj", type = ApiParamType.JSONARRAY, desc = "用户角色权限集合")
    })
    @Description(desc = "用户权限查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        String userUuid = jsonObj.getString("userUuid");
        List<UserAuthVo> userAuthList = userMapper.searchUserAuthByUserUuid(userUuid);
        List<RoleAuthVo> userRoleAuthList = userMapper.searchUserRoleAuthByUserUuid(userUuid);

        Map<String, String> userRoleAuthMap = new HashMap<>();

        JSONObject userRoleAuthObj = new JSONObject();

        if (userRoleAuthList != null && userRoleAuthList.size() > 0) {
            for (RoleAuthVo roleAuth : userRoleAuthList) {
                if (userRoleAuthMap.containsKey(roleAuth.getAuth())){
                    if (roleAuth.getAuthGroup().equals(userRoleAuthMap.get(roleAuth.getAuth()))){
                        continue;
                    }
                }
                userRoleAuthMap.put(roleAuth.getAuth(), roleAuth.getAuthGroup());
                if (userRoleAuthObj.containsKey(roleAuth.getAuthGroup())){
                    JSONArray authArray = userRoleAuthObj.getJSONArray(roleAuth.getAuthGroup());
                    authArray.add(roleAuth.getAuth());
                }else {
                    JSONArray authArray = new JSONArray();
                    authArray.add(roleAuth.getAuth());
                    userRoleAuthObj.put(roleAuth.getAuthGroup(), authArray);
                }
            }
        }

        JSONObject userAuthObj = new JSONObject();
        if (userAuthList != null && userAuthList.size() > 0) {
            for (UserAuthVo authVo : userAuthList) {
                boolean sameAuth = userRoleAuthMap.containsKey(authVo.getAuth());
                boolean sameGroup = false;
                if (sameAuth){
                    sameGroup = userRoleAuthMap.get(authVo.getAuth()).equals(authVo.getAuthGroup());
                }
                if (!sameAuth || !sameGroup) {
                    if (userAuthObj.containsKey(authVo.getAuthGroup())){
                        JSONArray authArray = userAuthObj.getJSONArray(authVo.getAuthGroup());
                        authArray.add(authVo.getAuth());
                    }else {
                        JSONArray authArray = new JSONArray();
                        authArray.add(authVo.getAuth());
                        userAuthObj.put(authVo.getAuthGroup(), authArray);
                    }
                }
            }
        }
        returnObj.put("userAuthObj", userAuthObj);
        returnObj.put("userRoleAuthObj", userRoleAuthObj);
        return returnObj;
    }
}
