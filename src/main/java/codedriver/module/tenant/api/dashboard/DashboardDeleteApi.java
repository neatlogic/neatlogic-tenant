package codedriver.module.tenant.api.dashboard;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dashboard.dao.mapper.DashboardMapper;
import codedriver.framework.dashboard.dto.DashboardVo;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.auth.label.DASHBOARD_MODIFY;
import codedriver.module.tenant.exception.dashboard.DashboardAuthenticationException;
import codedriver.module.tenant.exception.dashboard.DashboardNotFoundException;

@Service
@Transactional
@IsActived
public class DashboardDeleteApi extends ApiComponentBase {

	@Autowired
	private DashboardMapper dashboardMapper;
	
	@Autowired
	UserMapper userMapper;

	@Override
	public String getToken() {
		return "dashboard/delete";
	}

	@Override
	public String getName() {
		return "仪表板删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "仪表板uuid", isRequired = true) })
	@Description(desc = "仪表板删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String dashboardUuid = jsonObj.getString("uuid");
		DashboardVo dashboardVo = dashboardMapper.getDashboardByUuid(dashboardUuid);
		if (dashboardVo == null) {
			throw new DashboardNotFoundException(dashboardUuid);
		}
		List<UserAuthVo> userAuthList = userMapper.searchUserAllAuthByUserAuth(new UserAuthVo(UserContext.get().getUserId(),DASHBOARD_MODIFY.class.getSimpleName()));
		String userId = UserContext.get().getUserId();
		boolean hasRight = false;
		if (dashboardVo.getType().equals(DashboardVo.DashBoardType.CUSTOM.getValue())&&dashboardVo.getFcu().equals(userId)) {
			hasRight = true;
		}
		if (!hasRight&&dashboardVo.getType().equals(DashboardVo.DashBoardType.SYSTEM.getValue())
				&& CollectionUtils.isNotEmpty(userAuthList)) {
				hasRight = true;
		}
		if (!hasRight) {
			throw new DashboardAuthenticationException("管理");
		}
		dashboardMapper.deleteDashboardVisitCounterByDashboardUuid(dashboardUuid);
		dashboardMapper.deleteDashboardWidgetByDashboardUuid(dashboardUuid);
		dashboardMapper.deleteDashboardDefaultByDashboardUuid(dashboardUuid);
		dashboardMapper.deleteDashboardAuthorityByUuid(dashboardUuid);
		dashboardMapper.deleteDashboardByUuid(dashboardUuid);
		return null;
	}
}
