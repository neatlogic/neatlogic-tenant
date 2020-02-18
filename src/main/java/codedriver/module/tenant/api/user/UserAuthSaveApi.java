package codedriver.module.tenant.api.user;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.dto.UserVo;
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

@Service
public class UserAuthSaveApi extends ApiComponentBase {

    @Autowired
    private UserService userService;

    @Override
    public String getToken() {
        return "user/auth/save";
    }

    @Override
    public String getName() {
        return "用户权限保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "userId",
            type = ApiParamType.STRING,
            desc = "用户ID",
            isRequired = true),
            @Param(name = "userAuthList",
            type = ApiParamType.JSONARRAY,
            explode = UserAuthVo[].class,
            desc = "用户权限集合",
            isRequired = true)
    })
    @Description( desc = "用户权限保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        UserVo userVo = new UserVo();
        userVo.setUserId(jsonObj.getString("userId"));
        JSONArray userAuthArray = jsonObj.getJSONArray("userAuthList");
        List<UserAuthVo> userAuthVoList = new ArrayList<>();
        for (int i = 0; i < userAuthArray.size(); i++){
            JSONObject userAuthObj = userAuthArray.getJSONObject(i);
            UserAuthVo authVo = new UserAuthVo();
            authVo.setAuth(userAuthObj.getString("auth"));
            authVo.setAuthGroup(userAuthObj.getString("authGroup"));
            authVo.setUserId(jsonObj.getString("userId"));
            userAuthVoList.add(authVo);
        }
        userVo.setUserAuthList(userAuthVoList);
        userService.saveUserAuth(userVo);
        return null;
    }
}
