package codedriver.module.tenant.exception.dashboard;

import codedriver.framework.exception.core.ApiRuntimeException;

public class DashboardNameExistsException extends ApiRuntimeException {
	private static final long serialVersionUID = 4230545560652554738L;

	public DashboardNameExistsException(String uuid) {
		super("仪表板名称：" + uuid + "已存在");
	}

}
