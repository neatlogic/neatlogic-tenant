package codedriver.framework.tenant.api.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.AuthAction;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.service.RoleService;

@AuthAction(name="SYSTEM_ROLE_EDIT")
@Service
public class RoleSaveApi extends ApiComponentBase{

	@Autowired
	private RoleService roleService;
	
	@Override
	public String getToken() {
		return "role/roleSaveApi";
	}

	@Override
	public String getName() {
		return "保存角色信息接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "name", type = "String", desc = "角色名称"),
		@Param(name = "description", type = "String", desc = "角色描述")})
	@Output({@Param(name = "Status", type = "String", desc = "保存状态"),
		@Param(name = "name", type = "String", desc = "角色名称")})
	@Description(desc = "角色保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject json = new JSONObject();
		RoleVo roleVo = new RoleVo();
		roleVo.setName(jsonObj.getString("name"));
		roleVo.setDescription(jsonObj.getString("description"));
		try {
			roleService.saveRole(roleVo);
			json.put("name", roleVo.getName());
			json.put("Status", "OK");
		} catch (Exception e) {
			json.put("Status", "ERROR");
			json.put("Message", e.getMessage());
		}
		return json;
	}
}
