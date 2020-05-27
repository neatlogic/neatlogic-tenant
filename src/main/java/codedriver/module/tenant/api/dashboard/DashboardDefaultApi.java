package codedriver.module.tenant.api.dashboard;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
@IsActived
@Transactional
public class DashboardDefaultApi extends ApiComponentBase {

	@Autowired
	private DashboardMapper dashboardMapper;
	@Autowired
	UserMapper userMapper;

	@Override
	public String getToken() {
		return "dashboard/default";
	}

	@Override
	public String getName() {
		return "修改默认仪表板接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ 
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "仪表板uuid", isRequired = true), 
		@Param(name="type", type = ApiParamType.STRING, desc="默认类型，system|custom 默认custom"),
		@Param(name = "isDefault", type = ApiParamType.ENUM, rule = "1,0", desc = "是否设为默认，1或0", isRequired = true) 
	})
	@Description(desc = "修改默认仪表板接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String dashboardUuid = jsonObj.getString("uuid");
		String type = jsonObj.getString("type");
		if(StringUtils.isBlank(type)) {
			type = DashboardVo.DashBoardType.CUSTOM.getValue();
		}
		int isDefault = jsonObj.getIntValue("isDefault");
		DashboardVo dashboardVo = dashboardMapper.getDashboardByUuid(dashboardUuid);
		if (dashboardVo == null) {
			throw new DashboardNotFoundException(dashboardUuid);
		}
		String userUuid = UserContext.get().getUserUuid(true);
		List<UserAuthVo> userAuthList = userMapper.searchUserAllAuthByUserAuth(new UserAuthVo(userUuid,DASHBOARD_MODIFY.class.getSimpleName()));
		boolean hasRight = false;
		if (dashboardVo.getType().equals(DashboardVo.DashBoardType.CUSTOM.getValue())&&dashboardVo.getFcu().equals(userUuid)) {
			hasRight = true;
		}
		if (!hasRight&&(type.equals(DashboardVo.DashBoardType.SYSTEM.getValue())||dashboardVo.getType().equals(DashboardVo.DashBoardType.SYSTEM.getValue()))&& CollectionUtils.isNotEmpty(userAuthList)) {
			hasRight = true;
		}
		if (!hasRight) {
			throw new DashboardAuthenticationException("修改");
		}
		
		dashboardMapper.deleteDashboardDefaultByUserUuid(userUuid, type);
		if (isDefault == 1) {
			dashboardMapper.insertDashboardDefault(dashboardUuid, userUuid, type);
		}
		return null;
	}
}
