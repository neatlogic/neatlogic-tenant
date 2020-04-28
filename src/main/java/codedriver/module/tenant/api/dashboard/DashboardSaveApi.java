package codedriver.module.tenant.api.dashboard;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.label.DASHBOARD_MODIFY;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dashboard.dao.mapper.DashboardMapper;
import codedriver.framework.dashboard.dto.DashboardVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.dto.AuthorityVo;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.exception.dashboard.DashboardAuthenticationException;
import codedriver.module.tenant.exception.dashboard.DashboardNameExistsException;
import codedriver.module.tenant.exception.dashboard.DashboardParamException;

@Service
@Transactional
@IsActived
public class DashboardSaveApi extends ApiComponentBase {

	@Autowired
	private DashboardMapper dashboardMapper;

	@Autowired
	UserMapper userMapper;	
	@Autowired
	RoleMapper roleMapper;
	
	@Override
	public String getToken() {
		return "dashboard/save";
	}

	@Override
	public String getName() {
		return "仪表板保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "uuid", type = ApiParamType.STRING, desc = "仪表板uuid，为空代表新增"), 
			@Param(name = "name", xss = true, type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", desc = "仪表板名称"),
			@Param(name="type", type = ApiParamType.STRING, desc="分类类型，system|custom 默认custom"),
			@Param(name="valueList", type = ApiParamType.JSONARRAY, desc="授权列表，如果是system,则必填", isRequired = false),
			@Param(name = "widgetList", type = ApiParamType.JSONARRAY, desc = "组件列表，范例：\"chartType\": \"barchart\"," + "\"h\": 4," + "\"handler\": \"codedriver.module.process.dashboard.handler.ProcessTaskDashboardHandler\"," + "\"i\": 0," + "\"name\": \"组件1\"," + "\"refreshInterval\": 3," + "\"uuid\": \"aaaa\"," + "\"w\": 5," + "\"x\": 0," + "\"y\": 0") })
	@Output({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "仪表板uuid") })
	@Description(desc = "仪表板保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		DashboardVo dashboardVo = JSONObject.toJavaObject(jsonObj, DashboardVo.class);
		String uuid = jsonObj.getString("uuid");
		if((StringUtils.isBlank(uuid)&&StringUtils.isBlank(dashboardVo.getName()))) {
			throw new DashboardParamException("name");
		}
		String type = StringUtils.isBlank(jsonObj.getString("type"))?DashboardVo.DashBoardType.CUSTOM.getValue():jsonObj.getString("type");
		dashboardVo.setType(type);
		String userId = UserContext.get().getUserId(true);
		DashboardVo oldDashboardVo = new DashboardVo();
		if (StringUtils.isNotBlank(uuid)) {
			oldDashboardVo = dashboardMapper.getDashboardByUuid(dashboardVo.getUuid());
		}
		if((StringUtils.isNotBlank(dashboardVo.getName())&&!dashboardVo.getName().equals(oldDashboardVo.getName()))||StringUtils.isBlank(uuid)) {
			if (dashboardMapper.checkDashboardNameIsExists(dashboardVo) > 0) {
				throw new DashboardNameExistsException(dashboardVo.getName());
			}
		}
		if(StringUtils.isNotBlank(uuid)&&DashboardVo.DashBoardType.SYSTEM.getValue().equals(oldDashboardVo.getType())||DashboardVo.DashBoardType.SYSTEM.getValue().equals(dashboardVo.getType())) {
			//判断是否有管理员权限
			if(CollectionUtils.isEmpty(userMapper.searchUserAllAuthByUserAuth(new UserAuthVo(userId,DASHBOARD_MODIFY.class.getSimpleName())))&&CollectionUtils.isEmpty(roleMapper.getRoleByRoleNameList(UserContext.get().getRoleNameList()))) {
				throw new DashboardAuthenticationException("管理");
			}
			if(oldDashboardVo != null) {
				dashboardMapper.deleteDashboardAuthorityByUuid(oldDashboardVo.getUuid());
			}
		}
		if(type.equals(DashboardVo.DashBoardType.SYSTEM.getValue())) {
			if(CollectionUtils.isEmpty(dashboardVo.getValueList())) {
				throw new DashboardParamException("valueList");
			}
			//更新角色
			for(String value:dashboardVo.getValueList()) {
				AuthorityVo authorityVo = new AuthorityVo();
				if(value.toString().startsWith(GroupSearch.ROLE.getValuePlugin())) {
					authorityVo.setType(GroupSearch.ROLE.getValue());
					authorityVo.setUuid(value.toString().replaceAll(GroupSearch.ROLE.getValuePlugin(), StringUtils.EMPTY));
				}else if(value.toString().startsWith(GroupSearch.USER.getValuePlugin())) {
					authorityVo.setType(GroupSearch.USER.getValue());
					authorityVo.setUuid(value.toString().replaceAll(GroupSearch.USER.getValuePlugin(), StringUtils.EMPTY));
				}else {
					throw new DashboardParamException("valueList");
				}
				dashboardMapper.insertDashboardAuthority(authorityVo,dashboardVo.getUuid());
			}
		}else {
			if(StringUtils.isBlank(oldDashboardVo.getFcu())) {
				dashboardMapper.insertDashboardDefault(oldDashboardVo.getUuid(),userId,type);
			}
		}
		if(StringUtils.isBlank(uuid)) {
			dashboardVo.setFcu(userId);
			dashboardMapper.insertDashboard(dashboardVo);
		}else {
			dashboardVo.setLcu(userId);
			dashboardMapper.updateDashboard(dashboardVo);
			dashboardMapper.deleteDashboardWidgetByDashboardUuid(dashboardVo.getUuid());
		}
		if (dashboardVo.getWidgetList() != null && dashboardVo.getWidgetList().size() > 0) {
			for (DashboardWidgetVo widgetVo : dashboardVo.getWidgetList()) {
				widgetVo.setDashboardUuid(dashboardVo.getUuid());
				dashboardMapper.insertDashboardWidget(widgetVo);
			}
		}
		return dashboardVo.getUuid();
	}
}
