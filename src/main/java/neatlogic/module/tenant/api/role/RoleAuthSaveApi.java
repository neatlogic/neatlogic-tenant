package neatlogic.module.tenant.api.role;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dto.AuthVo;
import neatlogic.framework.dto.RoleAuthVo;
import neatlogic.framework.exception.role.RoleNotFoundException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import neatlogic.framework.auth.label.ROLE_MODIFY;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@AuthAction(action = ROLE_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class RoleAuthSaveApi extends PrivateApiComponentBase {
    
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
    	List<String> roleUuidList = JSON.parseArray(jsonObj.getString("roleUuidList"), String.class);
    	if(CollectionUtils.isNotEmpty(roleUuidList)) {
    		String action = jsonObj.getString("action");
            for (String roleUuid : roleUuidList){
            	if(roleMapper.checkRoleIsExists(roleUuid) == 0) {
            		throw new RoleNotFoundException(roleUuid);
            	}
                JSONObject roleAuthObj = jsonObj.getJSONObject("roleAuthList");
                List<RoleAuthVo> roleAuthList = new ArrayList<>();
                Set<String> keySet = roleAuthObj.keySet();
                for (String key : keySet){
                    JSONArray roleAuthArray = roleAuthObj.getJSONArray(key);
                    for (int j = 0; j < roleAuthArray.size(); j++){
                        RoleAuthVo roleAuthVo = new RoleAuthVo();
                        roleAuthVo.setAuth(roleAuthArray.getString(j));
                        roleAuthVo.setAuthGroup(key);
                        roleAuthVo.setRoleUuid(roleUuid);
                        roleAuthList.add(roleAuthVo);
                    }
                }

                if (AuthVo.AUTH_ADD.equals(action)){
                    List<RoleAuthVo> oldRoleAuthVoList = roleMapper.searchRoleAuthByRoleUuid(roleUuid);
            		Set<String> authSet = new HashSet<>();
            		for (RoleAuthVo authVo : oldRoleAuthVoList){
            			authSet.add(authVo.getAuth());
            		}
            		for (RoleAuthVo roleAuth : roleAuthList){
            			if (!authSet.contains(roleAuth.getAuth())){
            				roleMapper.insertRoleAuth(roleAuth);
            			}
            		}
                }else if(AuthVo.AUTH_COVER.equals(action)){
                    roleMapper.deleteRoleAuthByRoleUuid(roleUuid);
        			for (RoleAuthVo roleAuthVo : roleAuthList){
        				roleMapper.insertRoleAuth(roleAuthVo);
        			}
                }else if(AuthVo.AUTH_DELETE.equals(action)){
                	if(CollectionUtils.isEmpty(roleAuthList)) {
                    	for (RoleAuthVo roleAuth : roleAuthList){
                            roleMapper.deleteRoleAuth(roleAuth);
                    	}
                	}
                }
            }
    	}
        
        return null;
    }
}
