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
import codedriver.module.tenant.exception.user.UserPasswordException;

@AuthAction(name = "SYSTEM_USER_EDIT")
@Service
public class UserPasswordUpdateApi extends ApiComponentBase {
	
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
					isRequired = true)})
	@Output({})
	@Description(desc = "保存用户接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String userId = jsonObj.getString("userId");
		String password = jsonObj.getString("password");
		if(userId==null) {//管理员修改其他用户密码
			if(UserContext.get().getUserId()==null) {
				throw new UserPasswordException("当前用户未登录!");
			}else {
				userId = UserContext.get().getUserId();
			}
		}
		UserVo userVo = userMapper.getUserByUserId(userId);
		userVo.setPassword(password);		
		userMapper.updateUserPassword(userVo);
		return userVo.getUserId();
	}
}
