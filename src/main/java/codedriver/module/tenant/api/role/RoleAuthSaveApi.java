package codedriver.module.tenant.api.role;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dto.AuthVo;
import codedriver.framework.dto.RoleAuthVo;
import codedriver.framework.dto.RoleVo;
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
import java.util.Set;

@Service
public class RoleAuthSaveApi extends ApiComponentBase {


    @Autowired
    private RoleService roleService;
    
    @Autowired
    private RoleMapper roleMapper;

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
            @Param(name = "roleUuidList",
                    type = ApiParamType.JSONARRAY,
                    desc = "角色Uuid集合",
                    isRequired = true),
            @Param(name = "roleAuthList",
                    type = ApiParamType.JSONOBJECT,
                    desc = "角色权限集合",
                    isRequired = true),
            @Param(name = "action",
                    type = ApiParamType.STRING,
                    desc = "操作类型",
                    isRequired = true)
    })
    @Description( desc = "角色权限保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray roleUuidList = jsonObj.getJSONArray("roleUuidList");
        String action = jsonObj.getString("action");
        RoleVo roleVo = new RoleVo();
        for (int i = 0; i < roleUuidList.size() ; i++){
            roleVo.setUuid(roleUuidList.getString(i));
            JSONObject roleAuthObj = jsonObj.getJSONObject("roleAuthList");
            List<RoleAuthVo> roleAuthVoList = new ArrayList<>();
            Set<String> keySet = roleAuthObj.keySet();
            for (String key : keySet){
                JSONArray roleAuthArray = roleAuthObj.getJSONArray(key);
                for (int j = 0; j < roleAuthArray.size(); j++){
                    RoleAuthVo roleAuthVo = new RoleAuthVo();
                    roleAuthVo.setAuth(roleAuthArray.getString(j));
                    roleAuthVo.setAuthGroup(key);
                    roleAuthVo.setRoleUuid(roleVo.getUuid());
                    roleAuthVoList.add(roleAuthVo);
                }
            }
            roleVo.setRoleAuthList(roleAuthVoList);
            if (AuthVo.AUTH_ADD.equals(action)){
                roleService.addRoleAuth(roleVo);
            }
            if(AuthVo.AUTH_COVER.equals(action)){
                roleService.coverRoleAuth(roleVo);
            }
            if(AuthVo.AUTH_DELETE.equals(action)){
                roleMapper.deleteRoleAuth(roleVo);
            }
        }
        return null;
    }
}
