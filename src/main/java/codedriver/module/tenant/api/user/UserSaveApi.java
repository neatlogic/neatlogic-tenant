package codedriver.module.tenant.api.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.UserService;

@AuthAction(name = "SYSTEM_USER_EDIT")
@Service
public class UserSaveApi extends ApiComponentBase {

	@Autowired
	private UserService userService;

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

	@Input({
			@Param(name = "userId",
					type = ApiParamType.STRING,
					desc = "用户Id",
					isRequired = true,
                    xss = true),
			@Param(name = "userName",
					type = ApiParamType.STRING,
					desc = "用户姓名",
					isRequired = true,
					xss = true),
			@Param(name = "password",
					type = ApiParamType.STRING,
					desc = "用户密码",
					xss = true),
			@Param(name = "email",
					type = ApiParamType.STRING,
					desc = "用户邮箱",
					isRequired = false,
					xss = true),
			@Param(name = "phone",
					type = ApiParamType.STRING,
					desc = "用户电话",
					isRequired = false,
					xss = true),
			@Param(name = "isActive",
					type = ApiParamType.INTEGER,
					desc = "是否激活",
					isRequired = false),
			@Param(name = "teamUuidList",
					type = ApiParamType.JSONARRAY,
					desc = "组织uuid",
					isRequired = false),
			@Param(name = "roleNameList",
					type = ApiParamType.JSONARRAY,
					desc = "角色名称",
					isRequired = false),
			@Param(name = "userInfo",
				type = ApiParamType.STRING,
				desc = "其他信息",
				isRequired = false,
				xss = true),
			@Param(name = "userAuthList",
				type = ApiParamType.JSONOBJECT,
				desc = "权限列表"
			)/*,
			@Param(name = "saveMode",
					type = ApiParamType.ENUM,
					rule = "merge,replace",
					desc = "保存方式，merge：只更新提供的属性，replace：更新全部属性，不提供的置空")*/ })
	@Output({})
	@Description(desc = "保存用户接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		UserVo userVo = new UserVo();
		userVo.setUserId(jsonObj.getString("userId"));
		userVo.setUserName(jsonObj.getString("userName"));
		userVo.setPassword(jsonObj.getString("password"));
		userVo.setEmail(jsonObj.getString("email"));
		userVo.setPhone(jsonObj.getString("phone"));
		userVo.setIsActive(jsonObj.getInteger("isActive"));
		List<String> teamUuidList = new ArrayList<>();
		if (jsonObj.containsKey("teamUuidList")){
			JSONArray teamUuidArray = jsonObj.getJSONArray("teamUuidList");
			for (int i = 0; i < teamUuidArray.size(); i++){
				teamUuidList.add(teamUuidArray.getString(i));
			}
		}
		userVo.setTeamUuidList(teamUuidList);

		List<String> roleNameList = new ArrayList<>();
		if (jsonObj.containsKey("roleNameList")){
			JSONArray roleNameArray = jsonObj.getJSONArray("roleNameList");
			for (int i = 0; i < roleNameArray.size(); i++){
				roleNameList.add(roleNameArray.getString(i));
			}
		}
		userVo.setRoleNameList(roleNameList);
		userVo.setUserInfo(jsonObj.getString("userInfo"));
		List<UserAuthVo> userAuthVoList = new ArrayList<>();
		if (jsonObj.containsKey("userAuthList")){
			JSONObject userAuthObj = jsonObj.getJSONObject("userAuthList");
			Set<String> keySet = userAuthObj.keySet();
			for (String key : keySet){
				JSONArray authArray = userAuthObj.getJSONArray(key);
				for (int j = 0; j < authArray.size(); j++){
					UserAuthVo authVo = new UserAuthVo();
					authVo.setAuth(authArray.getString(j));
					authVo.setAuthGroup(key);
					authVo.setUserId(userVo.getUserId());
					userAuthVoList.add(authVo);
				}
			}
		}
		userVo.setUserAuthList(userAuthVoList);
		userService.saveUser(userVo);
		return userVo.getUserId();
	}
}
