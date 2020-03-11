package codedriver.module.tenant.api.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dashboard.core.DashboardHandlerFactory;
import codedriver.framework.dashboard.core.IDashboardHandler;
import codedriver.framework.dashboard.dao.mapper.DashboardMapper;
import codedriver.framework.dashboard.dto.ChartDataVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.exception.dashboard.DashboardHandlerNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.exception.dashboard.DashboardWidgetNotFoundException;

@Component
@IsActived
public class DashboardWidgetDataGetApi extends ApiComponentBase {

	@Autowired
	private DashboardMapper dashboardMapper;

	@Override
	public String getToken() {
		return "dashboard/widget/data/get";
	}

	@Override
	public String getName() {
		return "获取仪表板组件数据接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "仪表板组件uuid", isRequired = true) })
	@Output({ @Param(explode = ChartDataVo.class, desc = "数据集") })
	@Description(desc = "获取仪表板组件数据接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		DashboardWidgetVo widgetVo = dashboardMapper.getDashboardWidgetByUuid(jsonObj.getString("uuid"));
		if (widgetVo == null) {
			throw new DashboardWidgetNotFoundException(jsonObj.getString("uuid"));
		}
		IDashboardHandler handler = DashboardHandlerFactory.getHandler(widgetVo.getHandler());
		if (handler == null) {
			throw new DashboardHandlerNotFoundException(widgetVo.getHandler());
		}
		ChartDataVo chartDataVo = handler.getData(widgetVo);
		return chartDataVo;
	}
}
