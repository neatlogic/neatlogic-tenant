package codedriver.module.tenant.api.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
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
					isRequired = true),
			@Param(name = "userName",
					type = ApiParamType.STRING,
					desc = "用户姓名",
					isRequired = true),
			@Param(name = "pinyin",
				type = ApiParamType.STRING,
				desc = "姓名拼音",
				isRequired = false),
			@Param(name = "password",
					type = ApiParamType.STRING,
					desc = "用户密码",
					isRequired = true),
			@Param(name = "email",
					type = ApiParamType.STRING,
					desc = "用户邮箱",
					isRequired = false),
			@Param(name = "phone",
					type = ApiParamType.STRING,
					desc = "用户电话",
					isRequired = false),
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
					isRequired = true),
			@Param(name = "userInfo",
				type = ApiParamType.STRING,
				desc = "其他信息",
				isRequired = true),
			@Param(name = "saveMode",
					type = ApiParamType.ENUM,
					rule = "merge,replace",
					desc = "保存方式，merge：只更新提供的属性，replace：更新全部属性，不提供的置空") })
	@Output({})
	@Description(desc = "保存用户接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		UserVo userVo = new UserVo();
		userVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<UserVo>() {
		});

		// 保存角色
		List<String> roleList = JSON.parseArray(jsonObj.getString("roleList"), String.class);
		userVo.setRoleNameList(roleList);

		// 保存用户组
		List<String> teamUuidList = JSON.parseArray(jsonObj.getString("teamUuidList"), String.class);
		userVo.setTeamUuidList(teamUuidList);
		userService.saveUser(userVo);
		return userVo.getUserId();
	}
}
