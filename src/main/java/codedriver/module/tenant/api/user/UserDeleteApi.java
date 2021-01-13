package codedriver.module.tenant.api.user;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.auth.label.USER_MODIFY;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AuthAction(action = USER_MODIFY.class)
@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class UserDeleteApi extends PrivateApiComponentBase{
	
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
//    			userMapper.deleteUserByUuid(userUuid);
				userMapper.updateUserToDeleteByUuid(userUuid);
			}
    	}
		return null;
	}
}

