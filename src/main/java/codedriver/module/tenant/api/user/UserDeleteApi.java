package codedriver.module.tenant.api.user;

import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.UserService;

@AuthAction(name="SYSTEM_USER_EDIT")
@Service
public class UserDeleteApi extends ApiComponentBase{
	
	@Autowired
	private UserService userService;
	
	@Override
	public String getToken() {
		return "user/delete";
	}

	@Override
	public String getName() {
		return "删除用户接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	
	@Input({ @Param(name = "userIdList", type = ApiParamType.JSONARRAY, desc = "用户Id集合",isRequired=true)})
	@Output({})
	@Description(desc = "删除用户接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONArray idArray = jsonObj.getJSONArray("userIdList");
		for (int i = 0; i < idArray.size(); i++){
			String userId = idArray.getString(i);
			userService.deleteUserAuth(new UserVo(userId,null));
			userService.deleteUser(userId);
		}
		return null;
	}
}

