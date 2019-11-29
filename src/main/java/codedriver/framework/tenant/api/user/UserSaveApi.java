package codedriver.framework.tenant.api.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.api.core.ApiParamType;
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
public class UserSaveApi extends ApiComponentBase{
	
	@Autowired
	private UserAccountService userService;
	
	@Override
	public String getToken() {
		return "user/save";
	}

	@Override
	public String getName() {
		return "保存用户接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	
	@Input({ @Param(name = "userId", type = ApiParamType.STRING, desc = "用户Id",isRequired=true),
		@Param(name = "userName", type = ApiParamType.STRING, desc = "用户姓名",isRequired=true),
		@Param(name = "password", type = ApiParamType.STRING, desc = "用户密码",isRequired=true),
		@Param(name = "email", type = ApiParamType.STRING, desc = "用户邮箱",isRequired=false),
		@Param(name = "phone", type = ApiParamType.STRING, desc = "用户电话",isRequired=false),
		@Param(name = "company", type = ApiParamType.STRING, desc = "公司",isRequired=false),
		@Param(name = "dept", type = ApiParamType.STRING, desc = "部门",isRequired=false),
		@Param(name = "position", type = ApiParamType.STRING, desc = "职位",isRequired=false),
		@Param(name = "is_active", type = ApiParamType.LONG, desc = "是否激活",isRequired=false),
		@Param(name = "teamUuidList", type = ApiParamType.JSONARRAY, desc = "组织id,如有多个,逗号隔开",isRequired=false),
		@Param(name = "roleList", type = ApiParamType.JSONARRAY, desc = "角色名称,如有多个,逗号隔开",isRequired=true)})
	@Output({ @Param(name = "Status", type = ApiParamType.STRING, desc = "状态"),
		@Param(name = "userId", type = ApiParamType.STRING, desc = "保存的用户Id")
		})
	@Description(desc = "保存用户接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject json = new JSONObject();
		UserVo userVo = new UserVo();	
		userVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<UserVo>(){});
		
		//保存角色
		List<String> roleList = JSON.parseArray(jsonObj.getString("roleList"), String.class);
		userVo.setRoleList(roleList);
		
//		String roles = jsonObj.getString("roleList");
//		if (roles != null) {
//			List<String> roleNameList = new ArrayList<String>();
//			for (String role : roles.split(",")) {
//				roleNameList.add(role);
//			}
//			userVo.setRoleList(roleNameList);
//		}
		//保存用户组
		List<String> teamUuidList = JSON.parseArray(jsonObj.getString("teamUuidList"), String.class);
		userVo.setTeamUuidList(teamUuidList);
//		String teamIds = jsonObj.getString("teamUuidList");
//		if (teamIds != null) {
//			List<String> teamUuidList = new ArrayList<String>();
//			for (String teamUuid : teamIds.split(",")) {
//				teamUuidList.add(teamUuid);
//			}
//			userVo.setTeamUuidList(teamUuidList);
//		}
	
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

