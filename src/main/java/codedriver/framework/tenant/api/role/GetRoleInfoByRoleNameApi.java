package codedriver.framework.tenant.api.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.dto.RoleVo;
import codedriver.framework.tenant.service.RoleService;

@AuthAction(name="SYSTEM_ROLE_EDIT")
@Service
public class GetRoleInfoByRoleNameApi extends ApiComponentBase{

	@Autowired
	private RoleService roleService;
	
	@Override
	public String getToken() {
		return "role/getRoleInfoByRoleNameApi";
	}

	@Override
	public String getName() {
		return "根据角色名称查询角色信息";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "name", type = "String", desc = "角色名称,必填"),})
	@Output({@Param(name = "name", type = "String", desc = "角色名称"),
		@Param(name = "description", type = "String", desc = "角色描述")
		})
	@Description(desc = "角色查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject json = new JSONObject();
		if(jsonObj!=null && jsonObj.containsKey("name")) {			
			RoleVo roleVo = roleService.getRoleInfoByName(jsonObj.getString("name"));
			json.put("roleVo", roleVo);
		}else {
			json.put("Status", "ERROR");	
			json.put("Message", "请输入参数name");
		}
		return json;
	}
}
