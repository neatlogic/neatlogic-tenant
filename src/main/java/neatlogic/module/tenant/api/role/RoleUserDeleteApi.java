package neatlogic.module.tenant.api.role;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ROLE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dto.RoleUserVo;
import neatlogic.framework.exception.role.RoleNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
@AuthAction(action = ROLE_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class RoleUserDeleteApi extends PrivateApiComponentBase {

    @Resource
    private RoleMapper roleMapper;

	@Resource
	private UserService userService;

	@Override
	public String getToken() {
		return "role/user/delete";
	}

	@Override
	public String getName() {
		return "角色用户删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
        @Param( name = "roleUuid", isRequired = true, desc = "角色uuid", type = ApiParamType.STRING),
        @Param( name = "userUuidList", desc = "用户Uuid集合", type = ApiParamType.JSONARRAY)
	})
	@Description( desc = "角色用户删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String roleUuid = jsonObj.getString("roleUuid");
    	if(roleMapper.checkRoleIsExists(roleUuid) == 0) {
			throw new RoleNotFoundException(roleUuid);
		}
    	List<String> userUuidList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("userUuidList")), String.class);
    	if (CollectionUtils.isNotEmpty(userUuidList)){
    		for (String userUuid: userUuidList){
    			roleMapper.deleteRoleUser(new RoleUserVo(roleUuid, userUuid));
				//userService.updateUserCacheAndSessionByUserUuid(userUuid);
    		}
    	}
		return null;
	}

}
