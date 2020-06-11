package codedriver.module.tenant.api.role;

import codedriver.framework.dto.RoleAuthVo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.role.RoleNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

import java.util.List;
import java.util.Set;

@AuthAction(name = "SYSTEM_ROLE_EDIT")
@Service
@Transactional
public class RoleSaveApi extends ApiComponentBase {

	@Autowired
	RoleMapper roleMapper;

	@Override
	public String getToken() {
		return "role/save";
	}

	@Override
	public String getName() {
		return "角色信息保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "uuid",
					type = ApiParamType.STRING,
					desc = "角色uuid"),
			@Param(name = "name",
					type = ApiParamType.STRING,
					desc = "角色名称",
					isRequired = true, xss = true),
			@Param(name = "description",
					type = ApiParamType.STRING,
					desc = "角色描述", xss = true),
			@Param(name = "userUuidList",
					type = ApiParamType.JSONARRAY,
					desc = "用户uuid集合"),
			@Param( name= "roleAuthList",
					desc = "角色权限集合",
					type = ApiParamType.JSONOBJECT)})
	@Output({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "uuid")
	})
	@Description(desc = "角色信息保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		RoleVo roleVo = new RoleVo();
		roleVo.setName(jsonObj.getString("name"));
		roleVo.setDescription(jsonObj.getString("description"));
		String uuid = jsonObj.getString("uuid");		
		if (StringUtils.isNotBlank(uuid)) {
			if(roleMapper.checkRoleIsExists(uuid) == 0) {
				throw new RoleNotFoundException(uuid);
			}
			roleVo.setUuid(uuid);
			roleMapper.updateRole(roleVo);
		} else {
			roleMapper.insertRole(roleVo);
			List<String> userUuidList = JSON.parseArray(jsonObj.getString("userUuidList"), String.class);
			if (CollectionUtils.isNotEmpty(userUuidList)){
				UserVo userVo = new UserVo();
				userVo.setRoleUuid(roleVo.getUuid());
				for (String userUuid : userUuidList){
					userVo.setUuid(userUuid);
					roleMapper.insertRoleUser(userVo);
				}
			}

			JSONObject roleAuthObj = jsonObj.getJSONObject("roleAuthList");
			if (MapUtils.isNotEmpty(roleAuthObj)){
				RoleAuthVo roleAuthVo = new RoleAuthVo();
				roleAuthVo.setRoleUuid(roleVo.getUuid());
				Set<String> keySet = roleAuthObj.keySet();
				for (String key : keySet){
					roleAuthVo.setAuthGroup(key);
					JSONArray roleAuthArray = roleAuthObj.getJSONArray(key);
					for (int j = 0; j < roleAuthArray.size(); j++){
						roleAuthVo.setAuth(roleAuthArray.getString(j));
						roleMapper.insertRoleAuth(roleAuthVo);
					}
				}
			}
		}
		resultObj.put("uuid", roleVo.getUuid());
		return resultObj;
	}
}
