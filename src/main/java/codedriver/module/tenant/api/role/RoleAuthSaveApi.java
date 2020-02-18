package codedriver.module.tenant.api.role;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.RoleAuthVo;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.UserAuthVo;
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
public class RoleAuthSaveApi extends ApiComponentBase {


    @Autowired
    private RoleService roleService;

    @Override
    public String getToken() {
        return "role/auth/save";
    }

    @Override
    public String getName() {
        return "角色权限保存接口";
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
            @Param(name = "roleAuthList",
                    type = ApiParamType.JSONARRAY,
                    explode = RoleAuthVo[].class,
                    desc = "角色权限集合",
                    isRequired = true)
    })
    @Description( desc = "角色权限保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        RoleVo roleVo = new RoleVo();
        roleVo.setName(jsonObj.getString("roleName"));
        JSONArray roleAuthArray = jsonObj.getJSONArray("roleAuthList");
        List<RoleAuthVo> roleAuthVoList = new ArrayList<>();
        for (int i = 0; i < roleAuthArray.size(); i++){
            JSONObject roleAuthObj = roleAuthArray.getJSONObject(i);
            RoleAuthVo roleAuthVo = new RoleAuthVo();
            roleAuthVo.setAuth(roleAuthObj.getString("auth"));
            roleAuthVo.setAuthGroup(roleAuthObj.getString("authGroup"));
            roleAuthVo.setRoleName(jsonObj.getString("roleName"));
            roleAuthVoList.add(roleAuthVo);
        }
        roleVo.setRoleAuthList(roleAuthVoList);
        roleService.saveRoleAuth(roleVo);
        return null;
    }
}
