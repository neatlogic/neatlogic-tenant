package codedriver.framework.tenant.api.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.service.RoleService;

@AuthAction(name="SYSTEM_ROLE_EDIT")
@Service
public class RoleDeleteApi extends ApiComponentBase{

	@Autowired
	private RoleService roleService;
	
	@Override
	public String getToken() {
		return "role/delete";
	}

	@Override
	public String getName() {
		return "删除角色接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "name", type = ApiParamType.STRING, desc = "角色名称",isRequired=true)})
	@Output({@Param(name = "Status", type = ApiParamType.STRING, desc = "删除状态"),
		@Param(name = "name", type = ApiParamType.STRING, desc = "角色名称")})
	@Description(desc = "角色删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject json = new JSONObject();
		String name = jsonObj.getString("name");
		try {
			roleService.deleteRole(name);
			json.put("name", name);
			json.put("Status", "OK");
		} catch (Exception e) {
			json.put("Status", "ERROR");
			json.put("Message", e.getMessage());
		}
		return json;
	}
}
