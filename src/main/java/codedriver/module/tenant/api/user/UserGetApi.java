package codedriver.module.tenant.api.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.exception.user.UserGetException;

@AuthAction(name = "SYSTEM_USER_EDIT")
@Service
public class UserGetApi extends ApiComponentBase {

	@Autowired
	private UserMapper userMapper;

	@Override
	public String getToken() {
		return "user/get";
	}

	@Override
	public String getName() {
		return "根据用户id查询用户详情接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "userUuid", type = ApiParamType.STRING, desc = "用户uuid", isRequired = false)
	})
	@Output({
		@Param(name = "Return", explode = UserVo.class, desc = "用户详情")
	})
	@Description(desc = "根据用户Id查询用户详情")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String userUuid = jsonObj.getString("userUuid");
		if(userUuid == null) {
			if(StringUtils.isBlank(UserContext.get().getUserUuid())) {
				throw new UserGetException("当前用户未登录!");
			}else {
				userUuid = UserContext.get().getUserUuid();
			}
		}
		UserVo userVo = userMapper.getUserByUuid(userUuid);
		userVo.setUserAuthList(userMapper.searchUserAllAuthByUserAuth(new UserAuthVo(userUuid)));
		if(CollectionUtils.isNotEmpty(userVo.getTeamUuidList())) {
			List<String> teamUuidList = new ArrayList<>();
			for(String teamUuid : userVo.getTeamUuidList()) {
				teamUuidList.add(GroupSearch.TEAM.getValuePlugin() + teamUuid);
			}
			userVo.setTeamUuidList(teamUuidList);
		}
		if(CollectionUtils.isNotEmpty(userVo.getRoleUuidList())) {
			List<String> roleUuidList = new ArrayList<>();
			for(String roleUuid : userVo.getRoleUuidList()) {
				roleUuidList.add(GroupSearch.ROLE.getValuePlugin() + roleUuid);
			}
			userVo.setRoleUuidList(roleUuidList);
		}
		
		return userVo;
	}
}
