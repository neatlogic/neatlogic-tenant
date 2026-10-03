package neatlogic.module.tenant.api.auth;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthFactory;
import neatlogic.framework.auth.label.AUTHORITY_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.systemuser.ISystemUser;
import neatlogic.framework.common.constvalue.systemuser.SystemUserFactory;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserAuthVo;
import neatlogic.framework.exception.auth.AuthNotFoundException;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@AuthAction(action = AUTHORITY_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class AuthUserDeleteApi extends PrivateApiComponentBase {
    
    @Resource
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

    /** 删除所选用户的当前直接权限；系统分支先校验整批注册 UUID，不影响其他权限或成员。 */
	@Input({
        @Param( name = "auth", isRequired = true, desc = "权限", type = ApiParamType.STRING),
        @Param( name = "userUuidList", desc = "用户Uuid集合", type = ApiParamType.JSONARRAY),
        @Param( name = "userType", desc = "用户类型，默认普通用户", type = ApiParamType.STRING, rule = "user,system")
	})
	@Description( desc = "权限用户删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String auth = jsonObj.getString("auth");
    	if(AuthFactory.getAuthInstance(auth) == null) {
			throw new AuthNotFoundException(auth);
		}
    	List<String> userUuidList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("userUuidList")), String.class);
        if ("system".equalsIgnoreCase(jsonObj.getString("userType")) && CollectionUtils.isNotEmpty(userUuidList)) {
            Set<String> registeredUuidSet = new HashSet<>();
            for (ISystemUser systemUser : SystemUserFactory.getSystemUserList()) {
                registeredUuidSet.add(systemUser.getUserUuid());
            }
            for (String userUuid : userUuidList) {
                if (!registeredUuidSet.contains(userUuid)) {
                    throw new UserNotFoundException(userUuid);
                }
            }
        }
    	if (CollectionUtils.isNotEmpty(userUuidList)){
    		for (String userUuid: userUuidList){
    			userMapper.deleteUserAuth(new UserAuthVo(userUuid, auth));
    		}
    	}
		return null;
	}

}
