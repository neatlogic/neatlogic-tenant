package codedriver.module.tenant.api.dashboard;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dashboard.dao.mapper.DashboardMapper;
import codedriver.framework.dashboard.dto.DashboardRoleVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Component
@IsActived
public class DashboardRoleGetApi extends ApiComponentBase {

	@Autowired
	private DashboardMapper dashboardMapper;

	@Override
	public String getToken() {
		return "dashboard/role/get";
	}

	@Override
	public String getName() {
		return "获取仪表板权限接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "仪表板uuid", isRequired = true) })
	@Output({ @Param(name = "read", type = ApiParamType.JSONARRAY, desc = "仪表板只读权限列表"), @Param(name = "write", type = ApiParamType.JSONARRAY, desc = "仪表板编辑权限列表") })
	@Description(desc = "获取仪表板权限接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String dashboardUuid = jsonObj.getString("uuid");
		List<DashboardRoleVo> dashboardRoleList = dashboardMapper.getDashboardRoleByDashboardUuid(dashboardUuid);
		JSONObject returnObj = new JSONObject();
		JSONArray readList = new JSONArray();
		JSONArray writeList = new JSONArray();
		for (DashboardRoleVo dashboardRoleVo : dashboardRoleList) {
			if (dashboardRoleVo.getAction().equals(DashboardRoleVo.ActionType.READ.getValue())) {
				if (StringUtils.isNotBlank(dashboardRoleVo.getUserId())) {
					readList.add("user#" + dashboardRoleVo.getUserId());
				} else if (StringUtils.isNotBlank(dashboardRoleVo.getTeamUuid())) {
					readList.add("team#" + dashboardRoleVo.getTeamUuid());
				} else if (StringUtils.isNotBlank(dashboardRoleVo.getRoleName())) {
					readList.add("role#" + dashboardRoleVo.getRoleName());
				}
			} else if (dashboardRoleVo.getAction().equals(DashboardRoleVo.ActionType.WRITE.getValue())) {
				if (StringUtils.isNotBlank(dashboardRoleVo.getUserId())) {
					writeList.add("user#" + dashboardRoleVo.getUserId());
				} else if (StringUtils.isNotBlank(dashboardRoleVo.getTeamUuid())) {
					writeList.add("team#" + dashboardRoleVo.getTeamUuid());
				} else if (StringUtils.isNotBlank(dashboardRoleVo.getRoleName())) {
					writeList.add("role#" + dashboardRoleVo.getRoleName());
				}
			}
		}
		returnObj.put("read", readList);
		returnObj.put("write", writeList);
		return returnObj;
	}
}
