package codedriver.module.tenant.api.dashboard;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.label.DASHBOARD_MODIFY;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dashboard.dao.mapper.DashboardMapper;
import codedriver.framework.dashboard.dto.DashboardDefaultVo;
import codedriver.framework.dashboard.dto.DashboardVisitCounterVo;
import codedriver.framework.dashboard.dto.DashboardVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.exception.dashboard.DashboardNotFoundException;

@Component
@IsActived
public class DashboardGetApi extends ApiComponentBase {

	@Autowired
	private DashboardMapper dashboardMapper;

	@Autowired
	UserMapper userMapper;

	@Autowired
	TeamMapper teamMapper;

	@Override
	public String getToken() {
		return "dashboard/get";
	}

	@Override
	public String getName() {
		return "获取仪表板信息接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "仪表板uuid", isRequired = true) })
	@Output({ @Param(explode = DashboardVo.class, type = ApiParamType.JSONOBJECT, desc = "仪表板详细信息") })
	@Description(desc = "获取仪表板信息接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String dashboardUuid = jsonObj.getString("uuid");
		DashboardVo dashboardVo = new DashboardVo();
		dashboardVo.setUuid(dashboardUuid);
		String userId = UserContext.get().getUserId(true);
		dashboardVo.setFcu(userId);
		List<String> teamUuidList = teamMapper.getTeamUuidListByUserId(userId);
		dashboardVo.setUserId(userId);
		dashboardVo.setTeamUuidList(teamUuidList);
		dashboardVo.setRoleNameList(UserContext.get().getRoleNameList());
		dashboardVo = dashboardMapper.getAuthorizedDashboardByUuid(dashboardVo);
		if (dashboardVo == null) {
			throw new DashboardNotFoundException(dashboardUuid);
		}
		List<DashboardWidgetVo> dashboardWidgetList = dashboardMapper.getDashboardWidgetByDashboardUuid(dashboardUuid);
		List<UserAuthVo> userAuthList = userMapper.searchUserAllAuthByUserAuth(new UserAuthVo(UserContext.get().getUserId(),DASHBOARD_MODIFY.class.getSimpleName()));
		List<DashboardDefaultVo> dashboardDefaultList = dashboardMapper.getDefaultDashboardUuidByUserId(userId);
		dashboardVo.setWidgetList(dashboardWidgetList);
		// 补充权限数据
		if (CollectionUtils.isNotEmpty(dashboardDefaultList)) {
			if(dashboardDefaultList.stream().anyMatch(d->(d.getDashboardUuid().equals(dashboardUuid) && d.getType().equals(DashboardVo.DashBoardType.SYSTEM.getValue())))){
				dashboardVo.setIsSystemDefault(1);
			}else if(dashboardDefaultList.stream().anyMatch(d->(d.getDashboardUuid().equals(dashboardUuid) && d.getType().equals(DashboardVo.DashBoardType.CUSTOM.getValue())))){
				dashboardVo.setIsCustomDefault(1);
			}
		}
		if(dashboardVo.getType().equals(DashboardVo.DashBoardType.SYSTEM.getValue())
				&& CollectionUtils.isNotEmpty(userAuthList)) {
			dashboardVo.setIsCanEdit(1);
			dashboardVo.setIsCanRole(1);
		}else {
			if(UserContext.get().getUserId().equalsIgnoreCase(dashboardVo.getFcu())) {
				dashboardVo.setIsCanEdit(1);
				if(CollectionUtils.isNotEmpty(userAuthList)) {
					dashboardVo.setIsCanRole(1);
				}else {
					dashboardVo.setIsCanRole(0);
				}
			}else {
				dashboardVo.setIsCanEdit(0);
				dashboardVo.setIsCanRole(0);
			}
		}
		// 更新计数器
		DashboardVisitCounterVo counterVo = dashboardMapper.getDashboardVisitCounter(dashboardUuid, userId);
		if (counterVo == null) {
			dashboardMapper.insertDashboardVisitCounter(new DashboardVisitCounterVo(dashboardUuid, userId));
		} else {
			dashboardMapper.updateDashboardVisitCounter(counterVo);
		}
		return dashboardVo;
	}
}
