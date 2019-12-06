package codedriver.framework.tenant.api.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.service.UserService;

@AuthAction(name="SYSTEM_USER_EDIT")
@Service
public class UserBatchUpdateApi extends ApiComponentBase{
	
	@Autowired
	private UserService userService;
	
	@Override
	public String getToken() {
		return "user/batch/update";
	}

	@Override
	public String getName() {
		return "批量修改用户接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	
	@Input({ @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "是否启用，1：启用；0：禁用",isRequired=false),
		@Param(name = "userIdList", type = ApiParamType.JSONARRAY, desc = "用户Id集合",isRequired=true)})
	@Output({})
	@Description(desc = "批量修改用户接口,批量删除不用传'isActive',批量修改用户状态时必须传'isActive'")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<String> userIdList = JSON.parseArray(jsonObj.getString("userIdList"), String.class);
		if(jsonObj.containsKey("isActive")) {
			int isActive = jsonObj.getIntValue("isActive");
			userService.batchUpdateUser(isActive, userIdList);
		}else {
			userService.batchUpdateUser(null, userIdList);
		}
		return null;
	}
}


