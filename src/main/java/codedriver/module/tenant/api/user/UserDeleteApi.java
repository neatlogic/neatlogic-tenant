package codedriver.module.tenant.api.user;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@AuthAction(name="USER_MODIFY")
@Service
@Transactional
public class UserDeleteApi extends ApiComponentBase{
	
	@Autowired
	private UserMapper userMapper;
	
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
	
	
	@Input({ @Param(name = "userUuidList", type = ApiParamType.JSONARRAY, desc = "用户uuid集合",isRequired=true)})
	@Output({})
	@Description(desc = "删除用户接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<String> userUuidList = JSON.parseArray(jsonObj.getString("userUuidList"), String.class);
    	if(CollectionUtils.isNotEmpty(userUuidList)) {
    		for(String userUuid : userUuidList) {
    			userMapper.deleteUserAuth(new UserAuthVo(userUuid));
    			userMapper.deleteUserRoleByUserUuid(userUuid);
    			userMapper.deleteUserTeamByUserUuid(userUuid);
    			userMapper.deleteUserByUuid(userUuid);
    		}
    	}
		return null;
	}
}

