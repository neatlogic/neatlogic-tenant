package codedriver.module.tenant.api.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.exception.user.UserGetException;

@AuthAction(name = "SYSTEM_USER_EDIT")
@Service
public class UserGetApi extends ApiComponentBase {

	@Autowired
	private UserMapper userMapper;

	@Override
	public String getToken() {
		return "user/get";
	}

	@Override
	public String getName() {
		return "根据用户id查询用户详情接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "userId",
					type = ApiParamType.STRING,
					desc = "用户Id",
					isRequired = false)})
	@Output({
			@Param(name = "userId",
					type = ApiParamType.STRING,
					desc = "用户Id"),
			@Param(name = "userName",
					type = ApiParamType.STRING,
					desc = "用户姓名"),
			@Param(name = "email",
					type = ApiParamType.STRING,
					desc = "邮箱"),
			@Param(name = "phone",
					type = ApiParamType.STRING,
					desc = "电话"),
			@Param(name = "userInfo",
					type = ApiParamType.JSONOBJECT,
					desc = "其他属性"),
			@Param(name = "isActive",
					type = ApiParamType.INTEGER,
					desc = "是否激活(1:激活;0:未激活)"),
			@Param(name = "roleList",
					type = ApiParamType.JSONARRAY,
					desc = "用户角色信息列表"),
			@Param(name = "authList",
					type = ApiParamType.JSONARRAY,
					desc = "用户权限信息列表"),
			@Param(name = "teamList",
					type = ApiParamType.JSONARRAY,
					desc = "用户所在组信息列表")})
	@Description(desc = "根据用户Id查询用户详情")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String userId = jsonObj.getString("userId");
		if(userId==null) {
			if(UserContext.get().getUserId()==null) {
				throw new UserGetException("当前用户未登陆!");
			}else {
				userId = UserContext.get().getUserId();
			}
		}
		UserVo userVo = userMapper.getUserByUserId(userId);
		userVo.setUserAuthList(userMapper.searchUserAllAuthByUserAuth(new UserAuthVo(userId)));
		return userVo;
	}
}
