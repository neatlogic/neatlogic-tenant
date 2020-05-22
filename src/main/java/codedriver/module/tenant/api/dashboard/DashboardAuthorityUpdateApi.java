package codedriver.module.tenant.api.dashboard;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dashboard.dao.mapper.DashboardMapper;
import codedriver.framework.dashboard.dto.DashboardVo;
import codedriver.framework.dto.AuthorityVo;
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
import codedriver.module.tenant.exception.dashboard.DashboardParamException;

@Service
@Transactional
@IsActived
public class DashboardAuthorityUpdateApi extends ApiComponentBase {

	@Autowired
	private DashboardMapper dashboardMapper;

	@Autowired
	UserMapper userMapper;	
	@Autowired
	RoleMapper roleMapper;
	
	@Override
	public String getToken() {
		return "dashboard/authority/update";
	}

	@Override
	public String getName() {
		return "仪表板权限更新接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "uuid", type = ApiParamType.STRING, desc = "仪表板uuid，为空代表新增", isRequired = true), 
			@Param(name="type", type = ApiParamType.STRING, desc="分类类型，system|custom 默认custom"),
			@Param(name="valueList", type = ApiParamType.JSONARRAY, desc="授权列表，如果是system,则必填", isRequired = false)
	})
	@Output({ })
	@Description(desc = "仪表板权限更新接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		DashboardVo dashboardVo = JSONObject.toJavaObject(jsonObj, DashboardVo.class);
		String userUuid = UserContext.get().getUserUuid(true);
		DashboardVo oldDashboardVo = dashboardMapper.getDashboardByUuid(dashboardVo.getUuid());
		if(oldDashboardVo == null) {
			throw new DashboardNotFoundException(dashboardVo.getUuid());
		}
		if(DashboardVo.DashBoardType.SYSTEM.getValue().equals(dashboardVo.getType()) || DashboardVo.DashBoardType.SYSTEM.getValue().equals(oldDashboardVo.getType())) {
			//判断是否有管理员权限
			if(CollectionUtils.isEmpty(userMapper.searchUserAllAuthByUserAuth(new UserAuthVo(userUuid, DASHBOARD_MODIFY.class.getSimpleName())))) {
				throw new DashboardAuthenticationException("管理");
			}
			dashboardMapper.deleteDashboardAuthorityByUuid(oldDashboardVo.getUuid());
		}else if(!oldDashboardVo.getFcu().equals(userUuid)){
			throw new DashboardAuthenticationException("修改");
		}
		//修改分类类型
		oldDashboardVo.setType(dashboardVo.getType());
		oldDashboardVo.setLcu(userUuid);
		dashboardMapper.updateDashboard(oldDashboardVo);
		//更新权限
		if(DashboardVo.DashBoardType.SYSTEM.getValue().equals(dashboardVo.getType())&&CollectionUtils.isEmpty(dashboardVo.getValueList())) {
			throw new DashboardParamException("valueList");
		}
		if(DashboardVo.DashBoardType.SYSTEM.getValue().equals(dashboardVo.getType())){
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
		}
		return dashboardVo.getUuid();
	}
}
