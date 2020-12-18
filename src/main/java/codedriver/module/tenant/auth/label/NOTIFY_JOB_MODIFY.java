package codedriver.module.tenant.auth.label;

import codedriver.framework.auth.core.AuthBase;

public class NOTIFY_JOB_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "通知定时任务权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "通知定时任务权限";
	}

	@Override
	public String getAuthGroup() {
		return "framework";
	}
}
