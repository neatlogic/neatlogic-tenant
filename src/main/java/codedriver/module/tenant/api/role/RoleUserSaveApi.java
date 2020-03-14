package codedriver.module.tenant.api.role;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.RoleService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoleUserSaveApi extends ApiComponentBase {

    @Autowired
    private RoleService roleService;

    @Override
    public String getToken() {
        return "role/user/save";
    }

    @Override
    public String getName() {
        return "角色用户添加接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "roleName",
                    type = ApiParamType.STRING,
                    desc = "角色名称",
                    isRequired = true),
            @Param(name = "userIdList",
                    type = ApiParamType.JSONARRAY,
                    desc = "用户ID集合",
                    isRequired = true
            )})
    @Description(desc = "角色用户添加接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray userIdArray = jsonObj.getJSONArray("userIdList");
        String roleName = jsonObj.getString("roleName");
        List<String> userIdList = new ArrayList<>();
        for (int i = 0; i < userIdArray.size(); i++){
            userIdList.add(userIdArray.getString(i));
        }
        roleService.saveRoleUser(roleName, userIdList);
        return "";
    }
}
