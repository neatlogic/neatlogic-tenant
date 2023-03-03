package neatlogic.module.tenant.api.user;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.USER_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dao.mapper.UserSessionMapper;
import neatlogic.framework.dto.UserAuthVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@AuthAction(action = USER_MODIFY.class)
@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class UserDeleteApi extends PrivateApiComponentBase{
	
	@Resource
	private UserMapper userMapper;

	@Resource
	UserSessionMapper userSessionMapper;
	
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
    			userSessionMapper.deleteUserSessionByUserUuid(userUuid);
    			userMapper.deleteUserAuth(new UserAuthVo(userUuid));
    			userMapper.deleteUserRoleByUserUuid(userUuid);
    			userMapper.deleteUserTeamByUserUuid(userUuid);
    			userMapper.deleteUserByUuid(userUuid);
			}
    	}
		return null;
	}
}

