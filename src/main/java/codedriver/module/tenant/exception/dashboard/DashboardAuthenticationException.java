package codedriver.module.tenant.exception.dashboard;

import codedriver.framework.exception.core.ApiRuntimeException;

public class DashboardAuthenticationException extends ApiRuntimeException {
	private static final long serialVersionUID = 1997483988896454848L;

	public DashboardAuthenticationException(String action) {
		super("缺少：" + action + "权限，无法完成操作");
	}

}
