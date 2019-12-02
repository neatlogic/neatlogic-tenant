package codedriver.framework.tenant.api.user;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.AuthAction;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.service.UserAccountService;

@AuthAction(name="SYSTEM_USER_EDIT")
@Service
public class UserSearchByRoleApi extends ApiComponentBase{
	
	@Autowired
	private UserAccountService userService;
	
	@Override
	public String getToken() {
		return "user/search/role";
	}

	@Override
	public String getName() {
		return "根据角色查询用户";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	
	@Input({ @Param(name = "userId", type = ApiParamType.STRING, desc = "用户Id",isRequired=false),
		@Param(name = "roleName", type = ApiParamType.STRING, desc = "角色名称",isRequired=true)})
	@Output({ @Param(name = "userList", type = ApiParamType.JSONARRAY, desc = "用户信息list")})
	@Description(desc = "根据角色查询用户")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject json = new JSONObject();
		UserVo userVo = new UserVo();
		userVo.setUserId(jsonObj.getString("userId"));
		userVo.setRoleName(jsonObj.getString("roleName"));
		List<UserVo> userList = userService.getUserListByRole(userVo);
		json.put("userList",userList);
		return json;
	}
}
