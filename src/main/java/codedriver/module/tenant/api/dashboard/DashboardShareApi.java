package codedriver.module.tenant.api.dashboard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dashboard.dao.mapper.DashboardMapper;
import codedriver.framework.dashboard.dto.DashboardRoleVo;
import codedriver.framework.dashboard.dto.DashboardVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.role.RoleNotFoundException;
import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.exception.dashboard.DashboardAuthenticationException;

@Service
@IsActived
@Transactional
public class DashboardShareApi extends ApiComponentBase {

	@Autowired
	private DashboardMapper dashboardMapper;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private TeamMapper teamMapper;

	@Autowired
	private RoleMapper roleMapper;

	@Override
	public String getToken() {
		return "dashboard/share";
	}

	@Override
	public String getName() {
		return "仪表板共享接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "仪表板uuid"), @Param(name = "saveType", type = ApiParamType.ENUM, rule = "replace,merge", desc = "保存类型，replace（覆盖当前设置）或merge（和原来设置合并），默认replace"), @Param(name = "read", type = ApiParamType.JSONARRAY, desc = "只读权限列表，范例:[\"user#admin\",\"team#abcd\",\"role#R_ADMIN\"]"), @Param(name = "write", type = ApiParamType.JSONARRAY, desc = "编辑权限列表，范例:[\"user#admin\",\"team#abcd\",\"role#R_ADMIN\"]") })
	@Description(desc = "仪表板共享接口，如果权限列表都为空，则代表不再共享当前仪表板")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String dashboardUuid = jsonObj.getString("uuid");
		String saveType = "replace";
		if (jsonObj.containsKey("saveType")) {
			saveType = jsonObj.getString("saveType");
		}
		DashboardVo dashboardVo = dashboardMapper.getDashboardByUuid(dashboardUuid);
		String userId = UserContext.get().getUserId(true);
		if (!dashboardVo.getFcu().equals(userId)) {
			throw new DashboardAuthenticationException(DashboardRoleVo.ActionType.SHARE.getText());
		}
		List<DashboardRoleVo> dashboardRoleList = new ArrayList<>();
		Iterator<String> itKey = jsonObj.keySet().iterator();
		while (itKey.hasNext()) {
			String key = itKey.next();
			if (key.equals("read") || key.equals("write")) {
				JSONArray roleItemList = jsonObj.getJSONArray(key);
				if (roleItemList != null && roleItemList.size() > 0) {
					for (int i = 0; i < roleItemList.size(); i++) {
						String role = roleItemList.getString(i);
						DashboardRoleVo roleVo = new DashboardRoleVo();
						roleVo.setDashboardUuid(dashboardUuid);
						roleVo.setAction(key);
						if (role.startsWith("user#")) {
							String user = role.substring(5);
							if (StringUtils.isNotBlank(user)) {
								UserVo userVo = userMapper.getUserBaseInfoByUserId(user);
								if (userVo == null || userVo.getIsActive().equals(0)) {
									throw new UserNotFoundException(user);
								} else {
									roleVo.setUserId(user);
								}
							} else {
								continue;
							}
						} else if (role.startsWith("team#")) {
							String team = role.substring(5);
							if (StringUtils.isNotBlank(team)) {
								if (teamMapper.getTeamByUuid(team) == null) {
									throw new TeamNotFoundException(team);
								} else {
									roleVo.setTeamUuid(team);
								}
							} else {
								continue;
							}
						} else if (role.startsWith("role#")) {
							String r = role.substring(5);
							if (StringUtils.isNotBlank(r)) {
								if (roleMapper.getRoleByRoleName(r) == null) {
									throw new RoleNotFoundException(r);
								} else {
									roleVo.setRoleName(r);
								}
							} else {
								continue;
							}
						}
						dashboardRoleList.add(roleVo);
					}
				}
			}
		}
		if (saveType.equals("replace")) {
			dashboardMapper.deleteDashboardRoleByDashboardUuid(dashboardUuid);
			for (DashboardRoleVo roleVo : dashboardRoleList) {
				dashboardMapper.insertDashboardRole(roleVo);
			}
		} else if (saveType.equals("merge")) {
			List<DashboardRoleVo> oldRoleList = dashboardMapper.getDashboardRoleByDashboardUuid(dashboardUuid);
			for (DashboardRoleVo roleVo : dashboardRoleList) {
				if (!oldRoleList.contains(roleVo)) {
					dashboardMapper.insertDashboardRole(roleVo);
				}
			}
		}
		return null;
	}
}
