package codedriver.module.tenant.api.role;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import com.alibaba.fastjson.JSON;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dto.RoleUserVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@AuthAction(name = "SYSTEM_ROLE_EDIT")
@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class RoleDeleteApi extends ApiComponentBase {

	@Autowired
	private RoleMapper roleMapper;

	@Override
	public String getToken() {
		return "role/delete";
	}

	@Override
	public String getName() {
		return "角色删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "uuidList",
					type = ApiParamType.JSONARRAY,
					desc = "角色名称集合",
					isRequired = true) })
	@Description(desc = "角色删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<String> uuidList = JSON.parseArray(jsonObj.getString("uuidList"), String.class);
		for (String uuid : uuidList) {
			roleMapper.deleteMenuRoleByRoleUuid(uuid);
			roleMapper.deleteTeamRoleByRoleUuid(uuid);
			roleMapper.deleteRoleUser(new RoleUserVo(uuid));
			roleMapper.deleteRoleByUuid(uuid);
		}
		return null;
	}
}
