package codedriver.module.tenant.api.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dashboard.dao.mapper.DashboardMapper;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.exception.dashboard.DashboardWidgetNotFoundException;

@Service
@IsActived
public class DashboardWidgetGetApi extends ApiComponentBase {

	@Autowired
	private DashboardMapper dashboardMapper;

	@Override
	public String getToken() {
		return "dashboard/widget/get";
	}

	@Override
	public String getName() {
		return "获取仪表板组件接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "仪表板组件uuid", isRequired = true) })
	@Output({ @Param(explode = DashboardWidgetVo.class, type = ApiParamType.JSONOBJECT, desc = "仪表板组件详细信息") })
	@Description(desc = "获取仪表板组件接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		DashboardWidgetVo widgetVo = dashboardMapper.getDashboardWidgetByUuid(jsonObj.getString("uuid"));
		if (widgetVo == null) {
			throw new DashboardWidgetNotFoundException(jsonObj.getString("uuid"));
		}
		return widgetVo;
	}
}
