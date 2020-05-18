package codedriver.module.tenant.api.role;

import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@AuthAction(name = "SYSTEM_ROLE_EDIT")
@Service
@Transactional
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
		JSONArray uuidList = jsonObj.getJSONArray("uuidList");
		for (int i = 0; i < uuidList.size(); i++) {
			String uuid = uuidList.getString(i);
			roleMapper.deleteMenuRoleByRoleUuid(uuid);
			roleMapper.deleteTeamRoleByRoleUuid(uuid);
			roleMapper.deleteUserRoleByRoleUuid(uuid);
			roleMapper.deleteRoleByUuid(uuid);
		}
		return null;
	}
}
