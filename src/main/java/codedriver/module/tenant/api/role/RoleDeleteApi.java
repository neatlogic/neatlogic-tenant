package codedriver.module.tenant.api.role;

import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.RoleService;

@AuthAction(name = "SYSTEM_ROLE_EDIT")
@Service
public class RoleDeleteApi extends ApiComponentBase {

	@Autowired
	private RoleService roleService;

	@Override
	public String getToken() {
		return "role/delete";
	}

	@Override
	public String getName() {
		return "角色删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "nameList",
					type = ApiParamType.JSONARRAY,
					desc = "角色名称集合",
					isRequired = true) })
	@Description(desc = "角色删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONArray nameArray = jsonObj.getJSONArray("nameList");
		for (int i = 0; i < nameArray.size(); i++) {
			String name = nameArray.getString(i);
			roleService.deleteRoleByRoleName(name);
		}
		return null;
	}
}
