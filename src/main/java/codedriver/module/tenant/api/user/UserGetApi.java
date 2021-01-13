package codedriver.module.tenant.api.user;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.init.MaintenanceMode;
import codedriver.framework.common.config.Config;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class UserGetApi extends PrivateApiComponentBase {

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

	@Input({ @Param(name = "userUuid", type = ApiParamType.STRING, desc = "用户uuid", isRequired = false) })
	@Output({ @Param(name = "Return", explode = UserVo.class, desc = "用户详情") })
	@Description(desc = "根据用户Id查询用户详情")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String userUuid = jsonObj.getString("userUuid");
		if (StringUtils.isBlank(userUuid)) {
			userUuid = UserContext.get().getUserUuid(true);
		}
		UserVo userVo = null;
		//维护模式下 获取厂商维护人员信息
		if (Config.IS_MAINTENANCE_MODE() && MaintenanceMode.MAINTENANCE_USER.equals(UserContext.get().getUserId())) {
			userVo = MaintenanceMode.getMaintenanceUser();
		} else {
			userVo = userMapper.getUserByUuid(userUuid);
			if (userVo != null) {
				userVo.setUserAuthList(userMapper.searchUserAllAuthByUserAuth(new UserAuthVo(userUuid)));
			}
		}
		if (userVo == null) {
			throw new UserNotFoundException(userUuid);
		}
		JSONObject userJson = (JSONObject) JSON.toJSON(userVo);// 防止修改cache vo
		if (CollectionUtils.isNotEmpty(userJson.getJSONArray("teamUuidList"))) {
			List<String> teamUuidList = new ArrayList<>();
			for (Object teamUuid : userJson.getJSONArray("teamUuidList")) {
				teamUuidList.add(GroupSearch.TEAM.getValuePlugin() + teamUuid);
			}
			userJson.put("teamUuidList", teamUuidList);
		}
		if (CollectionUtils.isNotEmpty(userJson.getJSONArray("roleUuidList"))) {
			List<String> roleUuidList = new ArrayList<>();
			for (Object roleUuid : userJson.getJSONArray("roleUuidList")) {
				roleUuidList.add(GroupSearch.ROLE.getValuePlugin() + roleUuid);
			}
			userJson.put("roleUuidList", roleUuidList);
		}
		//告诉前端是否为维护模式
		userJson.put("isMaintenanceMode",0);
		if (Config.IS_MAINTENANCE_MODE()){
			userJson.put("isMaintenanceMode",1);
		}
		return userJson;
	}
}
