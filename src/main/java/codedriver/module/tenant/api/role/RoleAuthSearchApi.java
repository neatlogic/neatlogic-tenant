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
                    name = "roleNameList",
                    type = ApiParamType.JSONARRAY,
                    desc = "角色名称集合",
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
        JSONObject roleAuthObj = new JSONObject();
        JSONArray roleNameArray = jsonObj.getJSONArray("roleNameList");
        for (int i = 0 ; i < roleNameArray.size(); i++){
            List<RoleAuthVo> roleAuthList = roleService.searchRoleAuth(roleNameArray.getString(i));
            if (roleAuthList != null && roleAuthList.size() > 0){
                for (RoleAuthVo authVo : roleAuthList){
                    if (roleAuthObj.containsKey(authVo.getAuthGroup())){
                        JSONArray authArray = roleAuthObj.getJSONArray(authVo.getAuthGroup());
                        if (!authArray.contains(authVo.getAuth())){
                            authArray.add(authVo.getAuth());
                        }
                    }else {
                        JSONArray authArray = new JSONArray();
                        authArray.add(authVo.getAuth());
                        roleAuthObj.put(authVo.getAuthGroup(), authArray);
                    }
                }
            }
        }
        returnObj.put("roleAuthList", roleAuthObj);
        return returnObj;
    }
}
