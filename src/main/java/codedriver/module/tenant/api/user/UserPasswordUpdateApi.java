package codedriver.module.tenant.api.user;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
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
import codedriver.module.tenant.exception.user.UserCurrentPasswordException;

@AuthAction(name = "SYSTEM_USER_EDIT")
@Service
public class UserPasswordUpdateApi extends ApiComponentBase {
	
	@Autowired
	UserMapper userMapper;

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
			@Param(name = "password",
					type = ApiParamType.STRING,
					desc = "用户新密码",
					isRequired = true),
			@Param(name = "oldPassword",
			type = ApiParamType.STRING,
			desc = "用户当前密码",
			isRequired = true)
			
	})
	@Output({})
	@Description(desc = "保存用户接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String password = jsonObj.getString("password");
		String oldPassword = jsonObj.getString("oldPassword");
		String userId = UserContext.get().getUserId();
		UserVo oldUserVo = new UserVo();
		oldUserVo.setUserId(userId);
		oldUserVo.setPassword(oldPassword);
		UserVo userVo = userMapper.getUserByUserIdAndPassword(oldUserVo);
		if(userVo != null) {
			userVo.setPassword(password);		
			userMapper.updateUserPasswordActive(userId);
			List<Long> idList = userMapper.getLimitUserPasswordIdList(userId);
			if (idList != null && idList.size() > 0){
				userMapper.deleteUserPasswordByLimit(userId, idList);
			}
			userMapper.insertUserPassword(userVo);
		}else {
			throw new UserCurrentPasswordException();
		}
		return null;
	}
}
