package codedriver.module.tenant.api.auth;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.core.AuthFactory;
import codedriver.framework.auth.label.AUTHORITY_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dto.RoleAuthVo;
import codedriver.framework.exception.auth.AuthNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Transactional
@AuthAction(action = AUTHORITY_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class AuthRoleDeleteApi extends PrivateApiComponentBase {

    @Autowired
    private RoleMapper roleMapper;

	@Override
	public String getToken() {
		return "auth/role/delete";
	}

	@Override
	public String getName() {
		return "权限角色删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
        @Param( name = "auth", isRequired = true, desc = "权限", type = ApiParamType.STRING),
        @Param( name = "roleUuidList", desc = "角色Uuid集合", type = ApiParamType.JSONARRAY)
	})
	@Description( desc = "权限角色删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String auth = jsonObj.getString("auth");
    	if(AuthFactory.getAuthInstance(auth) == null) {
			throw new AuthNotFoundException(auth);
		}
    	List<String> roleUuidList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("roleUuidList")), String.class);
        if (CollectionUtils.isNotEmpty(roleUuidList)){
        	for (String roleUuid : roleUuidList){
        		roleMapper.deleteRoleAuth(new RoleAuthVo(roleUuid, auth));
        	}
        }
		return null;
	}

}
