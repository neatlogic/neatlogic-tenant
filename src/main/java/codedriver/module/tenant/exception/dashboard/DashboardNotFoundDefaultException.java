package codedriver.module.tenant.exception.dashboard;

import codedriver.framework.exception.core.ApiRuntimeException;

public class DashboardNotFoundDefaultException extends ApiRuntimeException {

	private static final long serialVersionUID = 339485229374813366L;

	public DashboardNotFoundDefaultException() {
		super("暂无默认首页，请设置默认首页");
	}

}
