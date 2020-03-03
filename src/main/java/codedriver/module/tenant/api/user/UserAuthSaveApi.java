package codedriver.module.tenant.api.user;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.AuthVo;
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
            @Param(name = "userIdList",
            type = ApiParamType.JSONARRAY,
            desc = "用户ID集合",
            isRequired = true),
            @Param(name = "userAuthList",
            type = ApiParamType.JSONARRAY,
            explode = UserAuthVo[].class,
            desc = "用户权限集合",
            isRequired = true),
            @Param(name = "action",
            type = ApiParamType.STRING,
            desc = "保存类型",
            isRequired = true)
    })
    @Description( desc = "用户权限保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray userIdArray = jsonObj.getJSONArray("userIdList");
        String action = jsonObj.getString("action");
        for (int i = 0; i < userIdArray.size(); i++){
            UserVo userVo = new UserVo();
            userVo.setUserId(userIdArray.getString(i));
            JSONArray userAuthArray = jsonObj.getJSONArray("userAuthList");
            List<UserAuthVo> userAuthVoList = new ArrayList<>();
            for (int j = 0; j < userAuthArray.size(); i++){
                JSONObject userAuthObj = userAuthArray.getJSONObject(j);
                UserAuthVo authVo = new UserAuthVo();
                authVo.setAuth(userAuthObj.getString("auth"));
                authVo.setAuthGroup(userAuthObj.getString("authGroup"));
                authVo.setUserId(userVo.getUserId());
                userAuthVoList.add(authVo);
            }
            userVo.setUserAuthList(userAuthVoList);
            if (AuthVo.AUTH_ADD.equals(action)){
                userService.addUserAuth(userVo);
            }
            if (AuthVo.AUTH_COVER.equals(action)){
                userService.coverUserAuth(userVo);
            }
            if (AuthVo.AUTH_DELETE.equals(action)){
                userService.deleteUserAuth(userVo);
            }
        }
        return null;
    }
}
