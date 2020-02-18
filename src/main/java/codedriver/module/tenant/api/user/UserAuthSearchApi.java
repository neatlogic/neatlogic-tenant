package codedriver.module.tenant.api.user;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.UserService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAuthSearchApi extends ApiComponentBase {

    @Autowired
    private UserService userService;

    @Override
    public String getToken() {
        return "user/auth/search ";
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
            @Param( name = "userAuthList", type = ApiParamType.JSONARRAY, explode = UserAuthVo[].class, desc = "用户权限集合")
    })
    @Description(desc = "用户权限查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        String userId = jsonObj.getString("userId");
        returnObj.put("userAuthList", userService.searchUserAuth(userId));
        return returnObj;
    }
}
