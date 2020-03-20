package codedriver.module.tenant.api.role;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
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
 * @create: 2020-03-20 10:35
 **/
@Service
public class RoleGetListApi extends ApiComponentBase {

    @Autowired
    private RoleService roleService;

    @Override
    public String getToken() {
        return "role/get/list";
    }

    @Override
    public String getName() {
        return "批量获取角色信息接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param( name = "roleNameList", desc = "角色名称集合", type = ApiParamType.JSONARRAY, isRequired = true)
    })
    @Output({
            @Param( name = "roleList", desc = "角色集合", type = ApiParamType.JSONARRAY, explode = RoleVo[].class)
    })
    @Description(desc = "批量获取角色信息接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        JSONArray roleNameArray = jsonObj.getJSONArray("roleNameList");
        List<RoleVo> roleList = new ArrayList<>();
        for (int i = 0; i < roleNameArray.size(); i++){
            String roleName = roleNameArray.getString(i);
            RoleVo roleVo = roleService.getRoleByRoleName(roleName);
            roleList.add(roleVo);
        }
        returnObj.put("roleList", roleList);
        return returnObj;
    }
}
