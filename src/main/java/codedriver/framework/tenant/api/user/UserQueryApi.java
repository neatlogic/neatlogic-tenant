package codedriver.framework.tenant.api.user;

import java.util.Map;

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
public class UserQueryApi extends ApiComponentBase{
	
	@Autowired
	private UserAccountService userService;
	
	@Override
	public String getToken() {
		return "user/userQueryApi";
	}

	@Override
	public String getName() {
		return "查询用户接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	
	@Input({ @Param(name = "userName", type = "String", desc = "关键字(用户id或名称),模糊查询"),
		@Param(name = "currentPage", type = "int", desc = "当前页数"),
		@Param(name = "pageSize", type = "int", desc = "每页展示数量 默认10")})
	@Output({ @Param(name = "userList", type = "JsonArray", desc = "用户信息list"),
		@Param(name = "pageCount", type = "int", desc = "总页数"),
		@Param(name = "currentPage", type = "int", desc = "当前页数"),
		@Param(name = "pageSize", type = "int", desc = "每页展示数量"),
		@Param(name = "userId", type = "String", desc = "用户Id"),
		@Param(name = "userName", type = "String", desc = "用户名"),
		@Param(name = "email", type = "String", desc = "邮箱"),
		@Param(name = "phone", type = "String", desc = "电话")
		})
	@Description(desc = "查询用户接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject json = new JSONObject();
		UserVo userVo = new UserVo();
		if(jsonObj!=null) {
			if(jsonObj.containsKey("userName")) {
				userVo.setUserName(jsonObj.getString("userName"));
			}
			if(jsonObj.containsKey("pageSize")) {
				userVo.setPageSize(jsonObj.getInteger("pageSize"));
			}
			if(jsonObj.containsKey("currentPage")) {
				userVo.setCurrentPage(jsonObj.getInteger("currentPage"));
			}
		}
		Map<String, Object> resultMap = userService.getUserList(userVo);
		json.put("userList", resultMap.get("resultList"));
		json.put("pageCount", resultMap.get("pageCount"));
		json.put("pageSize", userVo.getPageSize());
		json.put("currentPage", userVo.getCurrentPage());
		return json;
	}
}
