package codedriver.module.tenant.api.auth;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.UserService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-19 18:09
 **/
@Service
public class AuthUserSaveApi extends ApiComponentBase {


    @Autowired
    private UserService userService;

    @Override
    public String getToken() {
        return "auth/user/save";
    }

    @Override
    public String getName() {
        return "权限用户保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param( name = "auth", desc = "权限", type = ApiParamType.STRING, isRequired = true),
            @Param( name = "authGroup", desc = "权限组", type = ApiParamType.STRING, isRequired = true),
            @Param( name = "userIdList", desc = "用户ID集合", type = ApiParamType.JSONARRAY)
    })
    @Description(desc = "权限用户保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<UserAuthVo> userAuthList = new ArrayList<>();
        if (jsonObj.containsKey("userIdList")){
            JSONArray userIdArray = jsonObj.getJSONArray("userIdList");
            for (int i = 0; i < userIdArray.size(); i++){
                String userId = userIdArray.getString(i);
                UserAuthVo userAuthVo = new UserAuthVo();
                userAuthVo.setAuthGroup(jsonObj.getString("authGroup"));
                userAuthVo.setAuth(jsonObj.getString("auth"));
                userAuthVo.setUserId(userId);
                userAuthList.add(userAuthVo);
            }
        }
        userService.saveUserAuth(userAuthList, jsonObj.getString("auth"));
        return null;
    }
}
