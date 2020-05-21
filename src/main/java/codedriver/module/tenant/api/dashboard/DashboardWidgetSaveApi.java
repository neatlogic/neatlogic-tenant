package codedriver.module.tenant.api.dashboard;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dashboard.dao.mapper.DashboardMapper;
import codedriver.framework.dashboard.dto.DashboardVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.auth.label.DASHBOARD_MODIFY;
import codedriver.module.tenant.exception.dashboard.DashboardAuthenticationException;
import codedriver.module.tenant.exception.dashboard.DashboardNotFoundException;

@Service
@Transactional
@IsActived
public class DashboardWidgetSaveApi extends ApiComponentBase {

	@Autowired
	private DashboardMapper dashboardMapper;
	@Autowired
	UserMapper userMapper;	
	@Autowired
	RoleMapper roleMapper;

	@Override
	public String getToken() {
		return "dashboard/widget/save";
	}

	@Override
	public String getName() {
		return "保存仪表板组件接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "dashboardUuid", type = ApiParamType.STRING, desc = "仪表板uuid", isRequired = true),
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "组件uuid", isRequired = true),
		@Param(name = "name", type = ApiParamType.STRING, desc = "组件名称", isRequired = true),
		@Param(name = "refreshInterval", type = ApiParamType.INTEGER, desc = "组件定时刷新间隔，单位：秒，为0代表不定时刷新", isRequired = true),
		@Param(name = "handler", type = ApiParamType.STRING, desc = "组件处理类", isRequired = true),
		@Param(name = "chartType", type = ApiParamType.STRING, desc = "组件图表类型", isRequired = true),
		@Param(name = "conditionConfig", type = ApiParamType.STRING, desc = "数据过滤", isRequired = true),
		@Param(name = "chartConfig", type = ApiParamType.STRING, desc = "显示格式", isRequired = true),
		@Param(name = "x", type = ApiParamType.INTEGER, desc = "x坐标", isRequired = true),
		@Param(name = "y", type = ApiParamType.INTEGER, desc = "y坐标", isRequired = true),
		@Param(name = "i", type = ApiParamType.INTEGER, desc = "索引", isRequired = true),
		@Param(name = "w", type = ApiParamType.INTEGER, desc = "宽度", isRequired = true),
		@Param(name = "h", type = ApiParamType.INTEGER, desc = "高度", isRequired = true),
		
		})
	@Output({ @Param(explode = DashboardWidgetVo.class, type = ApiParamType.JSONOBJECT, desc = "仪表板组件详细信息") })
	@Description(desc = "保存仪表板组件接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String userUuid = UserContext.get().getUserUuid(true);
		DashboardWidgetVo dashboardWidgetVo = JSONObject.toJavaObject(jsonObj, DashboardWidgetVo.class);
		DashboardVo dashboardVo = dashboardMapper.getDashboardByUuid(dashboardWidgetVo.getDashboardUuid());
		if(dashboardVo == null) {
			throw new DashboardNotFoundException(dashboardWidgetVo.getDashboardUuid());
		}
		if(DashboardVo.DashBoardType.SYSTEM.getValue().equals(dashboardVo.getType())) {
			//判断是否有管理员权限
			if(CollectionUtils.isEmpty(userMapper.searchUserAllAuthByUserAuth(new UserAuthVo(userUuid,DASHBOARD_MODIFY.class.getSimpleName())))) {
				throw new DashboardAuthenticationException("管理");
			}
		}else if(!dashboardVo.getFcu().equalsIgnoreCase(userUuid)) {
			throw new DashboardAuthenticationException("修改");
		}
		
		dashboardMapper.deleteDashboardWidgetByUuid(dashboardWidgetVo.getDashboardUuid(),dashboardWidgetVo.getUuid());
		dashboardMapper.insertDashboardWidget(dashboardWidgetVo);
		return null;
	}
}
