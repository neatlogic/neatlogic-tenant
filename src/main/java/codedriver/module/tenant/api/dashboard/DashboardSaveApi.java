package codedriver.module.tenant.api.dashboard;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dashboard.dao.mapper.DashboardMapper;
import codedriver.framework.dashboard.dto.DashboardRoleVo;
import codedriver.framework.dashboard.dto.DashboardVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.exception.user.NoUserException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.exception.dashboard.DashboardAuthenticationException;
import codedriver.module.tenant.exception.dashboard.DashboardNameExistsException;

@Service
@Transactional
public class DashboardSaveApi extends ApiComponentBase {

	@Autowired
	private DashboardMapper dashboardMapper;

	@Override
	public String getToken() {
		return "dashboard/save";
	}

	@Override
	public String getName() {
		return "仪表板保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "仪表板uuid，为空代表新增"), @Param(name = "name", type = ApiParamType.STRING, desc = "仪表板名称", isRequired = true), @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "是否激活，1：激活，0：禁用", isRequired = true),
			@Param(name = "widgetList", type = ApiParamType.JSONARRAY, desc = "组件列表，范例：\"chartType\": \"barchart\"," + "\"h\": 4," + "\"handler\": \"codedriver.module.process.dashboard.handler.ProcessTaskDashboardHandler\"," + "\"i\": 0," + "\"name\": \"组件1\"," + "\"refreshInterval\": 3," + "\"uuid\": \"aaaa\"," + "\"w\": 5," + "\"x\": 0," + "\"y\": 0") })
	@Output({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "仪表板uuid") })
	@Description(desc = "仪表板保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		DashboardVo dashboardVo = JSONObject.toJavaObject(jsonObj, DashboardVo.class);
		if (dashboardMapper.checkDashboardNameIsExists(dashboardVo) > 0) {
			throw new DashboardNameExistsException(dashboardVo.getName());
		}
		String userId = UserContext.get().getUserId();
		if (StringUtils.isBlank(userId)) {
			throw new NoUserException();
		}
		DashboardVo oldDashboardVo = dashboardMapper.getDashboardByUuid(dashboardVo.getUuid());

		if (oldDashboardVo == null) {
			dashboardVo.setFcu(userId);
			dashboardMapper.insertDashboard(dashboardVo);
		} else {
			boolean hasRight = false;
			if (oldDashboardVo.getFcu().equals(userId)) {
				hasRight = true;
			}
			if (!hasRight) {
				List<String> roleList = dashboardMapper.getDashboardRoleByDashboardUuid(dashboardVo.getUuid(), userId);
				if (roleList.contains(DashboardRoleVo.ActionType.WRITE.getValue())) {
					hasRight = true;
				}
			}
			if (!hasRight) {
				throw new DashboardAuthenticationException(DashboardRoleVo.ActionType.WRITE.getText());
			}
			dashboardMapper.updateDashboard(dashboardVo);
			dashboardMapper.deleteDashboardWidgetByDashboardUuid(dashboardVo.getUuid());
		}
		if (dashboardVo.getWidgetList() != null && dashboardVo.getWidgetList().size() > 0) {
			for (DashboardWidgetVo widgetVo : dashboardVo.getWidgetList()) {
				widgetVo.setDashboardUuid(dashboardVo.getUuid());
				dashboardMapper.insertDashboardWidget(widgetVo);
			}
		}
		return dashboardVo.getUuid();
	}
}
