package codedriver.module.tenant.api.dashboard;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dashboard.dao.mapper.DashboardMapper;
import codedriver.framework.dashboard.dto.DashboardVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.exception.dashboard.DashboardNotFoundException;

@Service
public class DashboardGetApi extends ApiComponentBase {

	@Autowired
	private DashboardMapper dashboardMapper;

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
	@Output({ @Param(name = "dashboard", explode = DashboardVo.class, type = ApiParamType.JSONOBJECT, desc = "仪表板详细信息") })
	@Description(desc = "获取仪表板信息接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject jsonObject = new JSONObject();
		String dashboardUuid = jsonObj.getString("uuid");
		DashboardVo dashboardVo = dashboardMapper.getDashboardByUuid(dashboardUuid);
		if(dashboardVo==null) {
			throw new DashboardNotFoundException(dashboardUuid);
		}
		List<DashboardWidgetVo> dashboardWidgetList = dashboardMapper.getDashboardWidgetByDashboardUuid(dashboardUuid);
		dashboardVo.setWidgetList(dashboardWidgetList);
		return dashboardVo;
	}
}
