package codedriver.module.tenant.api.auth;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dto.RoleAuthVo;
import codedriver.framework.exception.role.RoleNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import codedriver.module.tenant.auth.label.AUTHORITY_MODIFY;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-19 18:10
 **/
@Service
@AuthAction(action = AUTHORITY_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class AuthRoleSaveApi extends PrivateApiComponentBase {

    @Autowired
    private RoleMapper roleMapper;

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
            @Param( name = "roleUuidList", desc = "角色uuid集合", type = ApiParamType.JSONARRAY)
    })
    @Description(desc = "权限角色保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	String auth = jsonObj.getString("auth");
    	roleMapper.deleteRoleAuthByAuth(auth);
    	String authGroup = jsonObj.getString("authGroup");

        List<String> roleUuidList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("roleUuidList")), String.class);
        if (CollectionUtils.isNotEmpty(roleUuidList)){
            RoleAuthVo roleAuthVo = new RoleAuthVo();
            roleAuthVo.setAuth(auth);
            roleAuthVo.setAuthGroup(authGroup);
            for (String roleUuid : roleUuidList){
            	if(roleMapper.checkRoleIsExists(roleUuid) == 0) {
            		throw new RoleNotFoundException(roleUuid);
            	}
                roleAuthVo.setRoleUuid(roleUuid);
                roleMapper.insertRoleAuth(roleAuthVo);
            }
        }
        return null;
    }
}
