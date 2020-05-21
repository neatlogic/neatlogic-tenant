package codedriver.module.tenant.api.dashboard;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dashboard.dao.mapper.DashboardMapper;
import codedriver.framework.dashboard.dto.DashboardDefaultVo;
import codedriver.framework.dashboard.dto.DashboardVisitCounterVo;
import codedriver.framework.dashboard.dto.DashboardVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.exception.dashboard.DashboardNotFoundDefaultException;
import codedriver.module.tenant.exception.dashboard.DashboardNotFoundException;

@Component
@IsActived
public class DashboardGetDefaultApi extends ApiComponentBase {

	@Autowired
	private DashboardMapper dashboardMapper;

	@Autowired
	UserMapper userMapper;
	
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
		String userUuid = UserContext.get().getUserUuid(true);
		String dashboardUuid = null;
		List<DashboardDefaultVo> dashboardDefaultList = dashboardMapper.getDefaultDashboardUuidByUserUuid(userUuid);
		if (CollectionUtils.isNotEmpty(dashboardDefaultList)) {
			for(DashboardDefaultVo dashboardDefaultVo:dashboardDefaultList) {
				if(dashboardDefaultVo.getType().equals(DashboardVo.DashBoardType.CUSTOM.getValue())) {
					dashboardUuid = dashboardDefaultVo.getDashboardUuid();
				}else if(dashboardDefaultVo.getType().equals(DashboardVo.DashBoardType.SYSTEM.getValue())&&StringUtils.isBlank(dashboardUuid)) {
					dashboardUuid = dashboardDefaultVo.getDashboardUuid();
				}
			}
		}
		if (StringUtils.isNotBlank(dashboardUuid)) {
			DashboardVo dashboardVo = dashboardMapper.getDashboardByUuid(dashboardUuid);
			if (dashboardVo == null) {
				throw new DashboardNotFoundException(dashboardUuid);
			}
			List<DashboardWidgetVo> dashboardWidgetList = dashboardMapper.getDashboardWidgetByDashboardUuid(dashboardUuid);
			dashboardVo.setWidgetList(dashboardWidgetList);
			// 更新计数器
			DashboardVisitCounterVo counterVo = dashboardMapper.getDashboardVisitCounter(dashboardUuid, userUuid);
			if (counterVo == null) {
				dashboardMapper.insertDashboardVisitCounter(new DashboardVisitCounterVo(dashboardUuid, userUuid));
			} else {
				dashboardMapper.updateDashboardVisitCounter(counterVo);
			}
			return dashboardVo;
		}else {
			throw new DashboardNotFoundDefaultException();
		}
	}
}
