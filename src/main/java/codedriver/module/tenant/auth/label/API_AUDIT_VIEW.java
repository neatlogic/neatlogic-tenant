package codedriver.module.tenant.auth.label;

import codedriver.framework.auth.core.AuthBase;

public class API_AUDIT_VIEW extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "查看操作审计权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "查看操作审计权限";
	}

	@Override
	public String getAuthGroup() {
		return "framework";
	}

	@Override
	public Integer sort() {
		return 8;
	}
}
