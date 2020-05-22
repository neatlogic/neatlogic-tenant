package codedriver.module.tenant.api.dashboard;

import org.apache.commons.lang3.StringUtils;
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
import codedriver.module.tenant.exception.dashboard.DashboardParamException;
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

	@Input({ 
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "仪表板组件uuid"),
		@Param(name = "chartType", type = ApiParamType.STRING, desc = "组件图表类型"),
		@Param(name = "conditionConfig", type = ApiParamType.STRING, desc = "数据过滤"),
		@Param(name = "handler", type = ApiParamType.STRING, desc = "组件处理类"),
		@Param(name = "chartConfig", type = ApiParamType.STRING, desc = "显示格式")
		})
	@Output({ @Param(explode = ChartDataVo.class, desc = "数据集") })
	@Description(desc = "获取仪表板组件数据接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		DashboardWidgetVo widgetVo = null;
		if(StringUtils.isNotBlank(uuid)) {
			widgetVo = dashboardMapper.getDashboardWidgetByUuid(uuid);
			if (widgetVo == null) {
				throw new DashboardWidgetNotFoundException(jsonObj.getString("uuid"));
			}
		}else {
			if(StringUtils.isBlank(jsonObj.getString("chartType"))) {
				throw new DashboardParamException("chartType");
			}
			if(StringUtils.isBlank(jsonObj.getString("conditionConfig"))) {
				throw new DashboardParamException("conditionConfig");
			}
			if(StringUtils.isBlank(jsonObj.getString("handler"))) {
				throw new DashboardParamException("handler");
			}
			if(StringUtils.isBlank(jsonObj.getString("chartConfig"))) {
				throw new DashboardParamException("chartConfig");
			}
			widgetVo = JSONObject.toJavaObject(jsonObj, DashboardWidgetVo.class);
		}
		IDashboardHandler handler = DashboardHandlerFactory.getHandler(widgetVo.getHandler());
		if (handler == null) {
			throw new DashboardHandlerNotFoundException(widgetVo.getHandler());
		}
		ChartDataVo chartDataVo = handler.getData(widgetVo);
		JSONObject chartDataJson =  (JSONObject) JSONObject.toJSON(chartDataVo);
		JSONObject dataJson = chartDataJson.getJSONObject("data");
		chartDataJson.remove("data");
		chartDataJson.put("theadList", dataJson.get("theadList"));
		chartDataJson.put("columnList", dataJson.get("columnList"));
		chartDataJson.put("dataList", dataJson.get("dataList"));
		chartDataJson.put("total", dataJson.get("total"));
		return chartDataJson;
	}
}
