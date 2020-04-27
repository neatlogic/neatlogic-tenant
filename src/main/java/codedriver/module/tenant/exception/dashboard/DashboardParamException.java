package codedriver.module.tenant.exception.dashboard;

import codedriver.framework.exception.core.ApiRuntimeException;

public class DashboardParamException extends ApiRuntimeException {
	private static final long serialVersionUID = -8922008245435838956L;

	public DashboardParamException(String name) {
		super("过滤条件参数 '" + name + "'非法");
	}
}
