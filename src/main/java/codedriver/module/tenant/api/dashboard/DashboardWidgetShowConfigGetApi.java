package codedriver.module.tenant.api.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dashboard.core.DashboardHandlerFactory;
import codedriver.framework.dashboard.core.IDashboardHandler;
import codedriver.framework.dashboard.dao.mapper.DashboardMapper;
import codedriver.framework.dashboard.dto.DashboardShowConfigVo;
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
public class DashboardWidgetShowConfigGetApi extends ApiComponentBase {

	@Autowired
	private DashboardMapper dashboardMapper;

	@Override
	public String getToken() {
		return "dashboard/widget/showconfig/get";
	}

	@Override
	public String getName() {
		return "获取仪表板展示格式配置接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ 
		@Param(name = "handler", type = ApiParamType.STRING, desc = "仪表板组件数据源", isRequired = true),
		@Param(name = "chartType", type = ApiParamType.STRING, desc = "仪表板组件类型", isRequired = true)
		})
	@Output({ 
		@Param(explode = DashboardShowConfigVo.class, desc = "仪表板展示格式配置")
		})
	@Description(desc = "获取仪表板展示格式配置接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String handlerStr = jsonObj.getString("handler");
		String chartType = jsonObj.getString("chartType");
		IDashboardHandler handler = DashboardHandlerFactory.getHandler(handlerStr);
		if (handler == null) {
			throw new DashboardHandlerNotFoundException(handlerStr);
		}
		DashboardWidgetVo widgetVo = new DashboardWidgetVo();
		widgetVo.setChartType(chartType);
		widgetVo.setHandler(handlerStr);
		return handler.getConfig(widgetVo);
	}
}
