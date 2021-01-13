package codedriver.module.tenant.api.role;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.role.RoleNotFoundException;
import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class RoleUserListApi extends PrivateApiComponentBase  {

	@Autowired
	private RoleMapper roleMapper;

	@Autowired
    private UserMapper userMapper;

	@Override
	public String getToken() {
		return "role/user/list";
	}

	@Override
	public String getName() {
		return "获取角色用户成员列表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
        @Param(name = "roleUuid", type = ApiParamType.STRING, isRequired = true, desc = "角色uuid")
	})
	@Output({
		@Param(name = "tbodyList", explode = UserVo[].class, desc = "角色用户成员列表")
	})
	@Description( desc = "获取角色用户成员列表")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String roleUuid = jsonObj.getString("roleUuid");
		if(roleMapper.checkRoleIsExists(roleUuid) == 0) {
			throw new RoleNotFoundException(roleUuid);
		}
		JSONObject resultObj = new JSONObject();
		List<UserVo> roleUserList = userMapper.getUserListByRoleUuid(roleUuid);
		resultObj.put("tbodyList", roleUserList);
		return resultObj;
	}

}
