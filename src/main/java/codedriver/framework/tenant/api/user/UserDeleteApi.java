package codedriver.framework.tenant.api.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.service.UserAccountService;

@AuthAction(name="SYSTEM_USER_EDIT")
@Service
public class UserDeleteApi extends ApiComponentBase{
	
	@Autowired
	private UserAccountService userService;
	
	@Override
	public String getToken() {
		return "user/userDeleteApi";
	}

	@Override
	public String getName() {
		return "删除用户接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	
	@Input({ @Param(name = "userId", type = "String", desc = "用户Id")})
	@Output({ @Param(name = "Status", type = "String", desc = "状态"),
			@Param(name = "userId", type = "String", desc = "删除的userId"),})
	@Description(desc = "删除用户接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject json = new JSONObject();
		if(jsonObj!=null && jsonObj.containsKey("userId")) {
			String userId = jsonObj.getString("userId");
			userService.deleteUser(userId);
			json.put("userId", userId);
			json.put("Status", "OK");
		}else {
			throw new RuntimeException("请输入参数userId");
		}
		return json;
	}
}

