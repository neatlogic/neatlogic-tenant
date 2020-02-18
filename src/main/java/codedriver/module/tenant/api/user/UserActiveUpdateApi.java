package codedriver.module.tenant.api.user;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.UserService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserActiveUpdateApi extends ApiComponentBase {

    @Autowired
    private UserService userService;

    @Override
    public String getToken() {
        return "user/active";
    }

    @Override
    public String getName() {
        return "用户有效性变更接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "userIdList", type = ApiParamType.JSONARRAY, desc = "用户Id集合",isRequired = true),
            @Param(name = "isActive", type = ApiParamType.STRING, desc = "有效性", isRequired = true)
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray userIdList = jsonObj.getJSONArray("userIdList");
        for (int i = 0; i < userIdList.size(); i++){
            UserVo userVo = new UserVo();
            userVo.setUserId(userIdList.getString(i));
            userVo.setIsActive(jsonObj.getInteger("isActive"));
            userService.updateUserActive(userVo);
        }
        return null;
    }
}
