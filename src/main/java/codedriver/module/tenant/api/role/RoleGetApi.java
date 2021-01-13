package codedriver.module.tenant.api.role;

import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.exception.role.RoleNotFoundException;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class RoleGetApi extends PrivateApiComponentBase {

	@Autowired
	private RoleMapper roleMapper;

	@Override
	public String getToken() {
		return "role/get";
	}

	@Override
	public String getName() {
		return "角色详细信息查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "uuid",
					type = ApiParamType.STRING,
					desc = "角色uuid",
					isRequired = true) })
	@Output({ @Param(explode = RoleVo.class) })
	@Description(desc = "角色详细信息查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		RoleVo roleVo = roleMapper.getRoleByUuid(uuid);
		if(roleVo == null) {
			throw new RoleNotFoundException(uuid);
		}
		int userCount = roleMapper.searchRoleUserCountByRoleUuid(uuid);
		roleVo.setUserCount(userCount);
		return roleVo;
	}
}
