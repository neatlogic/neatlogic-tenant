package codedriver.module.tenant.api.role;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.exception.role.RoleNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class RoleUserSaveApi extends ApiComponentBase {

    @Autowired
    private RoleMapper roleMapper;

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
            @Param(name = "roleUuid",
                    type = ApiParamType.STRING,
                    desc = "角色uuid",
                    isRequired = true),
            @Param(name = "userUuidList",
                    type = ApiParamType.JSONARRAY,
                    desc = "用户Uuid集合"
            )})
    @Description(desc = "角色用户添加接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String roleUuid = jsonObj.getString("roleUuid");
        if(roleMapper.checkRoleIsExists(roleUuid) == 0) {
			throw new RoleNotFoundException(roleUuid);
		}
        roleMapper.deleteUserRoleByRoleUuid(roleUuid);
		List<String> userUuidList = JSON.parseArray(jsonObj.getString("userUuidList"), String.class);
		if (CollectionUtils.isNotEmpty(userUuidList)){
			Set<String> userUuidSet = new HashSet<>(userUuidList);
			for (String userUuid : userUuidSet){
				roleMapper.insertRoleUser(userUuid, roleUuid);
			}
		}
        return null;
    }
}
