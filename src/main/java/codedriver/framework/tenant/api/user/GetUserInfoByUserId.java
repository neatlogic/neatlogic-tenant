package codedriver.framework.tenant.api.user;

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
import codedriver.framework.tenant.service.UserAccountService;

@AuthAction(name="SYSTEM_USER_EDIT")
@Service
public class GetUserInfoByUserId extends ApiComponentBase{
	
	@Autowired
	private UserAccountService userService;
	
	@Override
	public String getToken() {
		return "user/getUserInfoByUserId";
	}

	@Override
	public String getName() {
		return "根据用户id查询用户详情接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	
	@Input({ @Param(name = "userId", type = "Long", desc = "用户Id")})
	@Output({ @Param(name = "userId", type = "Long", desc = "用户Id"),
		@Param(name = "userName", type = "String", desc = "用户姓名"),
		@Param(name = "email", type = "String", desc = "邮箱"),
		@Param(name = "isActive", type = "int", desc = "是否激活(1:激活;0:未激活)"),
		@Param(name = "roleVoList", type = "int", desc = "是否激活(1:激活;0:未激活)"),
		@Param(name = "isActive", type = "int", desc = "是否激活(1:激活;0:未激活)"),
		@Param(name = "roleVoList", type = "JsonArray", desc = "用户角色信息"),
		@Param(name = "teamList", type = "JsonArray", desc = "用户所在组信息")
		})
	@Description(desc = "根据用户Id查询用户详情")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject json = new JSONObject();
		if(jsonObj!=null && jsonObj.containsKey("userId")) {			
			UserVo userVo = userService.getUserDetailByUserId(jsonObj.getString("userId"));
			json.put("userVo", userVo);
		}else {
			throw new RuntimeException("请输入参数userId");
		}
		
		return json;
	}
}

