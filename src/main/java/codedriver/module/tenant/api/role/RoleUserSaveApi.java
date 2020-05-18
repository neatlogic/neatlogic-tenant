package codedriver.module.tenant.api.role;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dto.UserVo;
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

import java.util.List;

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
            @Param(name = "roleName",
                    type = ApiParamType.STRING,
                    desc = "角色名称",
                    isRequired = true),
            @Param(name = "userUuidList",
                    type = ApiParamType.JSONARRAY,
                    desc = "用户Uuid集合"
            )})
    @Description(desc = "角色用户添加接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String roleName = jsonObj.getString("roleName");
        roleMapper.deleteUserRoleByRoleName(roleName);
		List<String> userUuidList = JSON.parseArray(jsonObj.getString("userUuidList"), String.class);
		if (CollectionUtils.isNotEmpty(userUuidList)){
			UserVo userVo = new UserVo();
			userVo.setRoleName(roleName);
			for (String userUuid : userUuidList){
				userVo.setUuid(userUuid);
				roleMapper.insertRoleUser(userVo);
			}
		}
        return null;
    }
}
