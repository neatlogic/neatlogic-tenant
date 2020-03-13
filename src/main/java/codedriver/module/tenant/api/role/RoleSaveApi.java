package codedriver.module.tenant.api.role;

import codedriver.framework.dto.RoleAuthVo;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.RoleService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AuthAction(name = "SYSTEM_ROLE_EDIT")
@Service
public class RoleSaveApi extends ApiComponentBase {

	@Autowired
	private RoleService roleService;

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
			@Param(name = "name",
					type = ApiParamType.STRING,
					desc = "角色名称",
					isRequired = true, xss = true),
			@Param(name = "description",
					type = ApiParamType.STRING,
					desc = "角色描述",
					isRequired = true, xss = true),
			@Param(name = "userIdList",
					type = ApiParamType.JSONARRAY,
					desc = "用户ID集合"),
			@Param( name= "roleAuthList",
					desc = "角色权限集合",
					type = ApiParamType.JSONARRAY)})
	@Description(desc = "角色信息保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		RoleVo roleVo = new RoleVo();
		roleVo.setName(jsonObj.getString("name"));
		roleVo.setDescription(jsonObj.getString("description"));
		List<String> userIdList = new ArrayList<>();
		if (jsonObj.containsKey("userIdList")){
			JSONArray userIdArray = jsonObj.getJSONArray("userIdList");
			for (int i = 0; i < userIdArray.size(); i++){
				userIdList.add(userIdArray.getString(i));
			}
		}
		roleVo.setUserIdList(userIdList);
		List<RoleAuthVo> roleAuthVoList = new ArrayList<>();
		if (jsonObj.containsKey("roleAuthList")){
			JSONObject roleAuthObj = jsonObj.getJSONObject("roleAuthList");
			Set<String> keySet = roleAuthObj.keySet();
			for (String key : keySet){
				JSONArray roleAuthArray = roleAuthObj.getJSONArray(key);
				for (int j = 0; j < roleAuthArray.size(); j++){
					RoleAuthVo roleAuthVo = new RoleAuthVo();
					roleAuthVo.setAuth(roleAuthArray.getString(j));
					roleAuthVo.setAuthGroup(key);
					roleAuthVo.setRoleName(roleVo.getName());
					roleAuthVoList.add(roleAuthVo);
				}
			}
		}
		roleVo.setRoleAuthList(roleAuthVoList);
		roleService.saveRole(roleVo);
		return null;
	}
}
