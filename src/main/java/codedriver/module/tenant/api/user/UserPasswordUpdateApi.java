package codedriver.module.tenant.api.user;

import java.util.List;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.NO_AUTH;
import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.module.tenant.exception.user.UserCurrentPasswordException;

@Service
@Transactional
@AuthAction(action = NO_AUTH.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class UserPasswordUpdateApi extends PrivateApiComponentBase {
	
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
	@Description(desc = "修改用户密码接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String password = jsonObj.getString("password");
		String oldPassword = jsonObj.getString("oldPassword");
		String userUuid = UserContext.get().getUserUuid(true);
		UserVo user = userMapper.getUserBaseInfoByUuid(userUuid);
		UserVo oldUserVo = new UserVo();
		oldUserVo.setUuid(userUuid);
		oldUserVo.setUserId(user.getUserId());
		oldUserVo.setPassword(oldPassword);
		UserVo userVo = userMapper.getUserByUserIdAndPassword(oldUserVo);
		if(userVo != null) {
			userVo.setPassword(password);		
			userMapper.updateUserPasswordActive(userUuid);
			List<Long> idList = userMapper.getLimitUserPasswordIdList(userUuid);
			if (idList != null && idList.size() > 0){
				userMapper.deleteUserPasswordByLimit(userUuid, idList);
			}
			userMapper.insertUserPassword(userVo);
		}else {
			throw new UserCurrentPasswordException();
		}
		return null;
	}
}
