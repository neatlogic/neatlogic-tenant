package codedriver.module.tenant.exception.dashboard;

import codedriver.framework.exception.core.ApiRuntimeException;

public class DashboardWidgetNotFoundException extends ApiRuntimeException {
	/**
	 * @Fields serialVersionUID : TODO
	 */
	private static final long serialVersionUID = 4236727381596990432L;

	public DashboardWidgetNotFoundException(String uuid) {
		super("仪表板组件：" + uuid + "不存在");
	}
}
