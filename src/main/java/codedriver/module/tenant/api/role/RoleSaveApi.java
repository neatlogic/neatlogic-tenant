package codedriver.module.tenant.api.role;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.RoleAuthVo;
import codedriver.framework.dto.RoleTeamVo;
import codedriver.framework.dto.RoleUserVo;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.exception.role.RoleNotFoundException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.auth.label.ROLE_MODIFY;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AuthAction(action = ROLE_MODIFY.class)
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class RoleSaveApi extends PrivateApiComponentBase {

	@Resource
	RoleMapper roleMapper;

	@Resource
	private UserMapper userMapper;

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
			@Param(name = "teamList",
					type = ApiParamType.JSONARRAY,
					desc = "分组集合，[{\"uuid\":\"aaaaaaaaaa\", \"checkedChildren\":1}]"),
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
			JSONArray userUuidArray = jsonObj.getJSONArray("userUuidList");
			if (CollectionUtils.isNotEmpty(userUuidArray)){
				List<String> userUuidList = userUuidArray.toJavaList(String.class);
				List<String> existUserUuidList = userMapper.checkUserUuidListIsExists(userUuidList,1);
				if(CollectionUtils.isNotEmpty(existUserUuidList)){
					for (String userUuid : existUserUuidList){
						roleMapper.insertRoleUser(new RoleUserVo(roleVo.getUuid(),userUuid));
					}
				}
			}
			JSONArray teamList = jsonObj.getJSONArray("teamList");
			if (CollectionUtils.isNotEmpty(teamList)) {
				List<RoleTeamVo> roleTeamList = new ArrayList<>(100);
				for (int i = 0; i < teamList.size(); i++) {
					JSONObject team = teamList.getJSONObject(i);
					if (team != null) {
						roleTeamList.add(new RoleTeamVo(roleVo.getUuid(), team.getString("uuid"), team.getInteger("checkedChildren")));
						if (roleTeamList.size() >= 100) {
							roleMapper.insertRoleTeamList(roleTeamList);
							roleTeamList.clear();
						}
					}
				}
				if (CollectionUtils.isNotEmpty(roleTeamList)) {
					roleMapper.insertRoleTeamList(roleTeamList);
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
