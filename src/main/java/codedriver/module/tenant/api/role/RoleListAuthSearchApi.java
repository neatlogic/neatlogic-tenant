package codedriver.module.tenant.api.role;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.RoleAuthVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.RoleService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 16:50
 **/
@Service
public class RoleListAuthSearchApi extends ApiComponentBase {

    @Autowired
    private RoleService roleService;

    @Override
    public String getToken() {
        return "role/list/auth/search";
    }

    @Override
    public String getName() {
        return "角色组权限查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input(@Param( name = "roleNameList", desc = "角色名称集合", type = ApiParamType.JSONARRAY, isRequired = true))
    @Output(@Param( name = "authList", desc = "权限集合", type = ApiParamType.JSONARRAY))
    @Description(desc = "角色组权限查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        Set<String> authSet = new HashSet<>();
        JSONArray roleNameArray = jsonObj.getJSONArray("roleNameList");
        for (int i = 0; i < roleNameArray.size(); i++){
            List<RoleAuthVo> roleAuthList = roleService.searchRoleAuth(roleNameArray.getString(i));
            if (CollectionUtils.isNotEmpty(roleAuthList)){
                for (RoleAuthVo roleAuth : roleAuthList){
                    authSet.add(roleAuth.getAuth());
                }
            }
        }
        returnObj.put("authList", authSet);
        return returnObj;
    }
}
