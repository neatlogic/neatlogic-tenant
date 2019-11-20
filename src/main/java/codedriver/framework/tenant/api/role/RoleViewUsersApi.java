package codedriver.framework.tenant.api.role;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.AuthAction;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.service.RoleService;

@AuthAction(name="SYSTEM_ROLE_EDIT")
@Service
public class RoleViewUsersApi extends ApiComponentBase{

	@Autowired
	private RoleService roleService;
	
	@Override
	public String getToken() {
		return "role/viewUsersApi";
	}

	@Override
	public String getName() {
		return "查询角色的用户详情";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "name", type = "String", desc = "角色名称"),})
	@Output({@Param(name = "userList", type = "JsonArray", desc = "用户信息")})
	@Description(desc = "查询该角色下用户信息")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject json = new JSONObject();
		UserVo vo = new UserVo();
		vo.setRoleName(jsonObj.getString("name"));
		List<UserVo> userList = roleService.viewUsers(vo);
		json.put("userList", userList);
		return json;
	}
}




