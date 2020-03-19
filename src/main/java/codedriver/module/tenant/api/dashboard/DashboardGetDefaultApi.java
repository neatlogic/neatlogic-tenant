package codedriver.module.tenant.api.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dashboard.dao.mapper.DashboardMapper;
import codedriver.framework.dashboard.dto.DashboardRoleVo;
import codedriver.framework.dashboard.dto.DashboardVisitCounterVo;
import codedriver.framework.dashboard.dto.DashboardVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.exception.dashboard.DashboardAuthenticationException;
import codedriver.module.tenant.exception.dashboard.DashboardNotFoundException;

@Component
@IsActived
public class DashboardGetDefaultApi extends ApiComponentBase {

	@Autowired
	private DashboardMapper dashboardMapper;

	@Override
	public String getToken() {
		return "dashboard/getdefault";
	}

	@Override
	public String getName() {
		return "获取默认仪表板接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Output({ @Param(explode = DashboardVo.class, type = ApiParamType.JSONOBJECT, desc = "仪表板详细信息") })
	@Description(desc = "获取默认仪表板接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String userId = UserContext.get().getUserId(true);
		String dashboardUuid = dashboardMapper.getDefaultDashboardUuidByUserId(userId);
		if (StringUtils.isNotBlank(dashboardUuid)) {
			DashboardVo dashboardVo = dashboardMapper.getDashboardByUuid(dashboardUuid);
			if (dashboardVo == null) {
				throw new DashboardNotFoundException(dashboardUuid);
			}

			boolean hasRight = false;
			if (dashboardVo.getFcu().equals(userId)) {
				hasRight = true;
				dashboardVo.setRoleList(new ArrayList<String>() {
					{
						this.add(DashboardRoleVo.ActionType.READ.getValue());
						this.add(DashboardRoleVo.ActionType.WRITE.getValue());
						this.add(DashboardRoleVo.ActionType.SHARE.getValue());
						this.add(DashboardRoleVo.ActionType.DELETE.getValue());
					}
				});
			}
			if (!hasRight) {
				List<String> roleList = dashboardMapper.getDashboardRoleByDashboardUuidAndUserId(dashboardVo.getUuid(), userId);
				dashboardVo.setRoleList(roleList);
				if (roleList.contains(DashboardRoleVo.ActionType.READ.getValue())) {
					hasRight = true;
				}
			}
			if (!hasRight) {
				throw new DashboardAuthenticationException(DashboardRoleVo.ActionType.READ.getText());
			}
			List<DashboardWidgetVo> dashboardWidgetList = dashboardMapper.getDashboardWidgetByDashboardUuid(dashboardUuid);
			dashboardVo.setWidgetList(dashboardWidgetList);
			// 更新计数器
			DashboardVisitCounterVo counterVo = dashboardMapper.getDashboardVisitCounter(dashboardUuid, userId);
			if (counterVo == null) {
				dashboardMapper.insertDashboardVisitCounter(new DashboardVisitCounterVo(dashboardUuid, userId));
			} else {
				dashboardMapper.updateDashboardVisitCounter(counterVo);
			}
			return dashboardVo;
		}
		return null;
	}
}