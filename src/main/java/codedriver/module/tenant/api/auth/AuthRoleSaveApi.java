package codedriver.module.tenant.api.auth;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.RoleAuthVo;
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

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-19 18:10
 **/
@Service
public class AuthRoleSaveApi extends ApiComponentBase {

    @Autowired
    private RoleService roleService;

    @Override
    public String getToken() {
        return "auth/role/save";
    }

    @Override
    public String getName() {
        return "权限角色保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param( name = "auth", desc = "权限", type = ApiParamType.STRING, isRequired = true),
            @Param( name = "authGroup", desc = "权限组", type = ApiParamType.STRING, isRequired = true),
            @Param( name = "roleNameList", desc = "角色集合", type = ApiParamType.JSONARRAY)
    })
    @Description(desc = "权限角色保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<RoleAuthVo> roleAuthVoList = new ArrayList();
        if (jsonObj.containsKey("roleNameList")){
            JSONArray roleNameArray = jsonObj.getJSONArray("roleNameList");
            for (int i = 0 ; i < roleNameArray.size(); i++){
                String roleName = roleNameArray.getString(i);
                RoleAuthVo roleAuthVo = new RoleAuthVo();
                roleAuthVo.setRoleName(roleName);
                roleAuthVo.setAuth(jsonObj.getString("auth"));
                roleAuthVo.setAuthGroup(jsonObj.getString("authGroup"));
                roleAuthVoList.add(roleAuthVo);
            }
        }
        roleService.saveAuthRole(roleAuthVoList, jsonObj.getString("auth"));
        return null;
    }
}
