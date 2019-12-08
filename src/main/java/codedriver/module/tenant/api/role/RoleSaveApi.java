package codedriver.module.tenant.api.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.AuthAction;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.RoleService;

@AuthAction(name = "SYSTEM_ROLE_EDIT")
@Service
public class RoleSaveApi extends ApiComponentBase {

	@Autowired
	private RoleService roleService;

	@Override
	public String getToken() {
		return "role/save";
	}

	@Override
	public String getName() {
		return "角色信息保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "name",
					type = ApiParamType.STRING,
					desc = "角色名称",
					isRequired = true),
			@Param(name = "description",
					type = ApiParamType.STRING,
					desc = "角色描述",
					isRequired = true) })
	@Description(desc = "角色信息保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		RoleVo roleVo = new RoleVo();
		roleVo.setName(jsonObj.getString("name"));
		roleVo.setDescription(jsonObj.getString("description"));
		roleService.saveRole(roleVo);
		return null;
	}
}
