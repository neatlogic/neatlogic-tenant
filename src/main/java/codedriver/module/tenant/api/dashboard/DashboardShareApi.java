package codedriver.module.tenant.api.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dashboard.dao.mapper.DashboardMapper;
import codedriver.framework.dashboard.dto.DashboardRoleVo;
import codedriver.framework.dashboard.dto.DashboardVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.exception.dashboard.DashboardAuthenticationException;

@Service
@Transactional
public class DashboardShareApi extends ApiComponentBase {

	@Autowired
	private DashboardMapper dashboardMapper;

	@Override
	public String getToken() {
		return "dashboard/share";
	}

	@Override
	public String getName() {
		return "仪表板共享接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "仪表板uuid"), @Param(name = "userIdList", type = ApiParamType.JSONARRAY, desc = "用户id列表"), @Param(name = "teamUuidList", type = ApiParamType.JSONARRAY, desc = "分组uuid列表"), @Param(name = "roleNameList", type = ApiParamType.JSONARRAY, desc = "角色名列表") })
	@Description(desc = "仪表板共享接口，如果用户id列表，分组uuid列表和角色名列表都为空，则代表不再共享当前仪表板")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String dashboardUuid = jsonObj.getString("uuid");
		DashboardVo dashboardVo = dashboardMapper.getDashboardByUuid(dashboardUuid);
		String userId = UserContext.get().getUserId(true);
		if (!dashboardVo.getFcu().equals(userId)) {
			throw new DashboardAuthenticationException(DashboardRoleVo.ActionType.SHARE.getText());
		}
//TODO 未完待续……
		return null;
	}
}
