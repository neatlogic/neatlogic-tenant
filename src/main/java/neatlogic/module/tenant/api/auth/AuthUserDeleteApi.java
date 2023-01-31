package neatlogic.module.tenant.api.auth;

import java.util.List;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.auth.label.AUTHORITY_MODIFY;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.auth.core.AuthFactory;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserAuthVo;
import neatlogic.framework.exception.auth.AuthNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
@Service
@Transactional
@AuthAction(action = AUTHORITY_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class AuthUserDeleteApi extends PrivateApiComponentBase {
    
    @Autowired
    private UserMapper userMapper;

	
	@Override
	public String getToken() {
		return "auth/user/delete";
	}

	@Override
	public String getName() {
		return "权限用户删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
        @Param( name = "auth", isRequired = true, desc = "权限", type = ApiParamType.STRING),
        @Param( name = "userUuidList", desc = "用户Uuid集合", type = ApiParamType.JSONARRAY)
	})
	@Description( desc = "权限用户删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String auth = jsonObj.getString("auth");
    	if(AuthFactory.getAuthInstance(auth) == null) {
			throw new AuthNotFoundException(auth);
		}
    	List<String> userUuidList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("userUuidList")), String.class);
    	if (CollectionUtils.isNotEmpty(userUuidList)){
    		for (String userUuid: userUuidList){
    			userMapper.deleteUserAuth(new UserAuthVo(userUuid, auth));
    		}
    	}
		return null;
	}

}
