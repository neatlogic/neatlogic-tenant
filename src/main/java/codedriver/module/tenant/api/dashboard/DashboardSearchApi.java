package codedriver.module.tenant.api.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dashboard.dao.mapper.DashboardMapper;
import codedriver.framework.dashboard.dto.DashboardRoleVo;
import codedriver.framework.dashboard.dto.DashboardVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Component
@IsActived
public class DashboardSearchApi extends ApiComponentBase {

	@Autowired
	private DashboardMapper dashboardMapper;

	@Override
	public String getToken() {
		return "dashboard/search";
	}

	@Override
	public String getName() {
		return "仪表板查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@SuppressWarnings("serial")
	@Input({ @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字"), @Param(name = "type", type = ApiParamType.ENUM, rule = "all,mine", desc = "类型，all或吗mine，默认值:all"), @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数", isRequired = false), @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页展示数量 默认20", isRequired = false), @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页") })
	@Output({ @Param(name = "pageCount", type = ApiParamType.INTEGER, desc = "总页数"), @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"), @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页展示数量"), @Param(name = "dashboardList", explode = DashboardVo[].class, desc = "仪表板列表") })
	@Description(desc = "仪表板查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		DashboardVo dashboardVo = new DashboardVo();
		if (jsonObj.containsKey("currentPage")) {
			dashboardVo.setCurrentPage(jsonObj.getInteger("currentPage"));
		}
		if (jsonObj.containsKey("pageSize")) {
			dashboardVo.setPageSize(jsonObj.getInteger("pageSize"));
		}
		String userId = UserContext.get().getUserId(true);
		dashboardVo.setFcu(userId);
		int rowNum = dashboardMapper.searchDashboardCount(dashboardVo);
		int pageCount = PageUtil.getPageCount(rowNum, dashboardVo.getPageSize());
		List<DashboardVo> dashboardList = dashboardMapper.searchDashboard(dashboardVo);
		String defaultDashboardUuid = dashboardMapper.getDefaultDashboardUuidByUserId(userId);
		// 补充权限数据
		for (DashboardVo dashboard : dashboardList) {
			if (StringUtils.isNotBlank(defaultDashboardUuid)) {
				if (dashboard.getUuid().equals(defaultDashboardUuid)) {
					dashboard.setIsDefault(1);
				}
			}
			if (dashboard.getFcu().equals(userId)) {
				// 自己创建的dashboard拥有所有权限
				dashboard.setRoleList(new ArrayList<String>() {
					{
						this.add(DashboardRoleVo.ActionType.READ.getValue());
						this.add(DashboardRoleVo.ActionType.WRITE.getValue());
						this.add(DashboardRoleVo.ActionType.SHARE.getValue());
						this.add(DashboardRoleVo.ActionType.DELETE.getValue());
					}
				});
			} else {
				dashboard.setRoleList(dashboardMapper.getDashboardRoleByDashboardUuid(dashboard.getUuid(), userId));
			}
		}

		JSONObject returnObj = new JSONObject();
		returnObj.put("rowNum", rowNum);
		returnObj.put("pageCount", pageCount);
		returnObj.put("currentPage", dashboardVo.getCurrentPage());
		returnObj.put("pageSize", dashboardVo.getPageSize());
		returnObj.put("dashboardList", dashboardList);
		return returnObj;
	}
}
