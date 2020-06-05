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
import codedriver.framework.dashboard.dao.mapper.DashboardMapper;
import codedriver.framework.dashboard.dto.DashboardVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.exception.dashboard.DashboardNotFoundException;
import codedriver.module.tenant.exception.dashboard.DashboardParamException;

@Service
@Transactional
@IsActived
public class DashboardCopyApi extends ApiComponentBase {

	@Autowired
	private DashboardMapper dashboardMapper;

	@Override
	public String getToken() {
		return "dashboard/copy";
	}

	@Override
	public String getName() {
		return "复制仪表板组件接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ 
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "仪表板uuid", isRequired = true) ,
		@Param(name = "name", type = ApiParamType.STRING, desc = "复制仪表板name", isRequired = true) 
		})
	@Output({ @Param(explode=DashboardVo.class) })
	@Description(desc = "复制仪表板组件接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		DashboardVo dashboardVo = JSONObject.toJavaObject(jsonObj, DashboardVo.class);
		DashboardVo oldDashboardVo = dashboardMapper.getDashboardByUuid(dashboardVo.getUuid());
		if(oldDashboardVo == null) {
			throw new DashboardNotFoundException(dashboardVo.getUuid());
		}
		
		//修改dashboard
		oldDashboardVo.setName(dashboardVo.getName());
		oldDashboardVo.setFcu(UserContext.get().getUserUuid());
		oldDashboardVo.setFcd(null);
		oldDashboardVo.setLcu(null);
		oldDashboardVo.setLcd(null);
		updateWidgetList(oldDashboardVo);
		dashboardMapper.insertDashboard(oldDashboardVo);
		return oldDashboardVo;
		
	}
	/**
	 * 更新组件配置
	 * @param dashboardVo
	 */
	private void updateWidgetList(DashboardVo dashboardVo) {
		List<DashboardWidgetVo> dashboardWidgetList =dashboardMapper.getDashboardWidgetByDashboardUuid(dashboardVo.getUuid());
		dashboardVo.setUuid(null);
		if(CollectionUtils.isNotEmpty(dashboardWidgetList)) {
			for(DashboardWidgetVo widgetVo : dashboardWidgetList) {
				if(StringUtils.isBlank(widgetVo.getChartConfig())) {
					throw new DashboardParamException("widgetList.chartConfig");
				}
				widgetVo.setDashboardUuid(dashboardVo.getUuid());
				widgetVo.setUuid(null);
				dashboardMapper.insertDashboardWidget(widgetVo);
			}
		}
	}
}
