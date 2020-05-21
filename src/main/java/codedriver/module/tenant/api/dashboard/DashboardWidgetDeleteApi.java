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
public class DashboardWidgetDeleteApi extends ApiComponentBase {

	@Autowired
	private DashboardMapper dashboardMapper;
	@Autowired
	UserMapper userMapper;	
	@Autowired
	RoleMapper roleMapper;

	@Override
	public String getToken() {
		return "dashboard/widget/delete";
	}

	@Override
	public String getName() {
		return "删除仪表板组件接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "dashboardUuid", type = ApiParamType.STRING, desc = "仪表板uuid", isRequired = true),
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "组件uuid", isRequired = true)
		})
	@Output({})
	@Description(desc = "删除仪表板组件接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String userId = UserContext.get().getUserId(true);
		String dashboardUuid = jsonObj.getString("dashboardUuid");
		String uuid = jsonObj.getString("uuid");
		DashboardVo dashboardVo = dashboardMapper.getDashboardByUuid(dashboardUuid);
		if(dashboardVo == null) {
			throw new DashboardNotFoundException(uuid);
		}
		if(DashboardVo.DashBoardType.SYSTEM.getValue().equals(dashboardVo.getType())) {
			//判断是否有管理员权限
			if(CollectionUtils.isEmpty(userMapper.searchUserAllAuthByUserAuth(new UserAuthVo(userId,DASHBOARD_MODIFY.class.getSimpleName())))) {
				throw new DashboardAuthenticationException("管理");
			}
		}else if(!dashboardVo.getFcu().equalsIgnoreCase(userId)) {
			throw new DashboardAuthenticationException("修改");
		}
		
		dashboardMapper.deleteDashboardWidgetByUuid(dashboardUuid,uuid);
		return null;
	}
}
