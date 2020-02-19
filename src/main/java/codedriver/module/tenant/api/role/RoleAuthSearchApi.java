package codedriver.module.tenant.api.role;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.RoleAuthVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.RoleService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleAuthSearchApi extends ApiComponentBase {

    @Autowired
    private RoleService roleService;

    @Override
    public String getToken() {
        return "role/auth/search";
    }

    @Override
    public String getName() {
        return "角色权限查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(
                    name = "roleName",
                    type = ApiParamType.STRING,
                    desc = "角色名称",
                    isRequired = true
            )
    })
    @Output({
            @Param(
                    name = "roleAuthList",
                    type = ApiParamType.JSONARRAY,
                    explode = RoleAuthVo[].class,
                    desc = "角色权限集合"
            )
    })
    @Description(desc = "角色权限查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        String roleName = jsonObj.getString("roleName");
        returnObj.put("roleAuthList",  roleService.searchRoleAuth(roleName));
        return returnObj;
    }
}
