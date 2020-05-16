package codedriver.module.tenant.api.dashboard;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.dashboard.Chart;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@IsActived
@Transactional
public class DashboardChartListApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "dashboard/chart/list";
	}

	@Override
	public String getName() {
		return "获取仪表板组件接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ 
		
	})
	@Output({ 
		@Param(name="",desc="")
	})
	@Description(desc = "修改默认仪表板接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return Chart.getChartList();
	}
}
