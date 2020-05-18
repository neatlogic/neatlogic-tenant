package codedriver.module.tenant.api.user;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.util.UuidUtil;

@AuthAction(name = "SYSTEM_USER_EDIT")
@Service
@Transactional
public class UserSaveApi extends ApiComponentBase {

	@Autowired
	UserMapper userMapper;

	@Autowired
	TeamMapper teamMapper;

	@Override
	public String getToken() {
		return "user/save";
	}

	@Override
	public String getName() {
		return "保存用户接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "uuid",
				type = ApiParamType.STRING,
				desc = "用户uuid", 
				maxLength = 32,
				minLength = 32),
			@Param(name = "userId",
					type = ApiParamType.STRING,
					desc = "用户Id",
					isRequired = true,
                    xss = true),
			@Param(name = "userName",
					type = ApiParamType.STRING,
					desc = "用户姓名",
					isRequired = true,
					xss = true),
			@Param(name = "password",
					type = ApiParamType.STRING,
					desc = "用户密码",
					xss = true),
			@Param(name = "email",
					type = ApiParamType.STRING,
					desc = "用户邮箱",
					isRequired = false,
					xss = true),
			@Param(name = "phone",
					type = ApiParamType.STRING,
					desc = "用户电话",
					isRequired = false,
					xss = true),
			@Param(name = "isActive",
					type = ApiParamType.INTEGER,
					desc = "是否激活",
					isRequired = false),
			@Param(name = "teamUuidList",
					type = ApiParamType.JSONARRAY,
					desc = "组织uuid",
					isRequired = false),
			@Param(name = "roleNameList",
					type = ApiParamType.JSONARRAY,
					desc = "角色名称",
					isRequired = false),
			@Param(name = "userInfo",
				type = ApiParamType.STRING,
				desc = "其他信息",
				isRequired = false,
				xss = true),
			@Param(name = "userAuthList",
				type = ApiParamType.JSONOBJECT,
				desc = "权限列表"
			)/*,
			@Param(name = "saveMode",
					type = ApiParamType.ENUM,
					rule = "merge,replace",
					desc = "保存方式，merge：只更新提供的属性，replace：更新全部属性，不提供的置空")*/ })
	@Output({})
	@Description(desc = "保存用户接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		UserVo userVo = new UserVo();		
		userVo.setUserId(jsonObj.getString("userId"));
		userVo.setUserName(jsonObj.getString("userName"));
		userVo.setPassword(jsonObj.getString("password"));
		userVo.setEmail(jsonObj.getString("email"));
		userVo.setPhone(jsonObj.getString("phone"));
		userVo.setIsActive(jsonObj.getInteger("isActive"));
		userVo.setUserInfo(jsonObj.getString("userInfo"));	
		
		String uuid = jsonObj.getString("uuid");
		if(StringUtils.isBlank(uuid)) {
			userVo.setUuid(UuidUtil.getUuid());
			userMapper.insertUser(userVo);
			userMapper.insertUserPassword(userVo);
			JSONObject userAuthObj = jsonObj.getJSONObject("userAuthList");
			if(MapUtils.isNotEmpty(userAuthObj)) {
				Set<String> keySet = userAuthObj.keySet();
				for (String key : keySet){
					JSONArray authArray = userAuthObj.getJSONArray(key);
					for (int j = 0; j < authArray.size(); j++){
						UserAuthVo authVo = new UserAuthVo();
						authVo.setAuth(authArray.getString(j));
						authVo.setAuthGroup(key);
						authVo.setUserUuid(userVo.getUuid());
						userMapper.insertUserAuth(authVo);
					}
				}
			}		
		}else {
			if(userMapper.getUserBaseInfoByUuid(uuid) == null) {
				throw new UserNotFoundException(uuid);
			}
			userVo.setUuid(uuid);
			userMapper.updateUser(userVo);
			userMapper.deleteUserRoleByUserUuid(userVo.getUuid());
			userMapper.deleteUserTeamByUserUuid(userVo.getUuid());
			//更新密码
			if (StringUtils.isNotBlank(userVo.getPassword())){
				userMapper.updateUserPasswordActive(userVo.getUuid());
				List<Long> idList = userMapper.getLimitUserPasswordIdList(userVo.getUuid());
				if (CollectionUtils.isNotEmpty(idList)){
					userMapper.deleteUserPasswordByLimit(userVo.getUuid(), idList);
				}
				userMapper.insertUserPassword(userVo);
			}
		}
		
		List<String> teamUuidList = JSON.parseArray(jsonObj.getString("teamUuidList"), String.class);
		if(CollectionUtils.isNotEmpty(teamUuidList)) {
			for(String teamUuid : teamUuidList) {
				userMapper.insertUserTeam(userVo.getUuid(), teamUuid.replaceAll(GroupSearch.TEAM.getValuePlugin(), StringUtils.EMPTY));
			}
		}
		
		List<String> roleNameList = JSON.parseArray(jsonObj.getString("roleNameList"), String.class);
		if(CollectionUtils.isNotEmpty(roleNameList)) {
			for(String roleName : roleNameList) {
				userMapper.insertUserRole(userVo.getUuid(), roleName);
			}
		}
		
		return userVo.getUuid();
	}
}
