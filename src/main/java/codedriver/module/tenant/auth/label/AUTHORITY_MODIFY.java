package codedriver.module.tenant.auth.label;

import codedriver.framework.auth.core.AuthBase;

public class AUTHORITY_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "权限管理权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "对权限进行添加、修改和删除";
	}

	@Override
	public String getAuthGroup() {
		return "framework";
	}
}
