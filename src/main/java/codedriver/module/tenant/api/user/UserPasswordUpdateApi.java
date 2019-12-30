package codedriver.module.tenant.api.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@AuthAction(name = "SYSTEM_USER_EDIT")
@Service
public class UserPasswordUpdateApi extends ApiComponentBase {
	
	private static final String USER_TYPE_ADMIN = "admin";
	
	@Autowired
	private UserMapper userMapper;

	@Override
	public String getToken() {
		return "user/password/update";
	}

	@Override
	public String getName() {
		return "修改用户密码接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "userId",
					type = ApiParamType.STRING,
					desc = "用户Id",
					isRequired = false),
			@Param(name = "password",
					type = ApiParamType.STRING,
					desc = "用户密码",
					isRequired = true),
			@Param(name = "type",
				type = ApiParamType.ENUM,
				rule = "admin,user",
				isRequired = true,
				desc = "修改方式，admin：管理员修改，user：用户修改")})
	@Output({})
	@Description(desc = "保存用户接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String userId = null;
		String password = jsonObj.getString("password");
		if(USER_TYPE_ADMIN.equals(jsonObj.getString("type"))) {//管理员修改其他用户密码
			userId = jsonObj.getString("userId");
		}else {//用户自行修改密码
			userId = UserContext.get().getUserId();
		}
		
		UserVo userVo = userMapper.getUserByUserId(userId);
		userVo.setPassword(password);		
		userMapper.updateUserPassword(userVo);
		return userVo.getUserId();
	}
}
