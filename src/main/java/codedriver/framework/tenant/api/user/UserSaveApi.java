package codedriver.framework.tenant.api.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.AuthAction;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.service.UserAccountService;

@AuthAction(name="SYSTEM_USER_EDIT")
@Service
public class UserSaveApi extends ApiComponentBase{
	
	@Autowired
	private UserAccountService userService;
	
	@Override
	public String getToken() {
		return "user/userSaveApi";
	}

	@Override
	public String getName() {
		return "保存用户接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	
	@Input({ @Param(name = "userId", type = "String", desc = "用户Id"),
		@Param(name = "userName", type = "String", desc = "用户姓名"),
		@Param(name = "password", type = "String", desc = "用户密码"),
		@Param(name = "email", type = "String", desc = "用户邮箱"),
		@Param(name = "is_active", type = "int", desc = "是否激活"),
		@Param(name = "teamIdList", type = "String", desc = "组织id,如有多个,逗号隔开"),
		@Param(name = "roleList", type = "String", desc = "角色名称,如有多个,逗号隔开"),})
	@Description(desc = "保存用户接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject json = new JSONObject();
		UserVo userVo = new UserVo();
		if(jsonObj!=null) {
			userVo.setUserId(jsonObj.getString("userId"));
			userVo.setUserName(jsonObj.getString("userName"));
			userVo.setPassword(jsonObj.getString("password"));
			userVo.setEmail(jsonObj.getString("email"));
			userVo.setIsActive(jsonObj.getInteger("isActive"));
			//保存角色
			String roles = jsonObj.getString("roleList");
			if (roles != null) {
				List<String> roleNameList = new ArrayList<String>();
				for (String role : roles.split(",")) {
					roleNameList.add(role);
				}
				userVo.setRoleList(roleNameList);
			}
			//保存用户组
			String teamIds = jsonObj.getString("teamIdList");
			if (teamIds != null) {
				List<Long> teamIdList = new ArrayList<Long>();
				for (String teamId : teamIds.split(",")) {
					teamIdList.add(Long.valueOf(teamId));
				}
				userVo.setTeamIdList(teamIdList);
			}
		}

		try {
			userService.saveUser(userVo);
			json.put("userId", userVo.getUserId());
			json.put("Status", "OK");
		} catch (Exception e) {
			json.put("Status", "ERROR");
			json.put("Message", e.getMessage());
		}
		return json;
	}
}

