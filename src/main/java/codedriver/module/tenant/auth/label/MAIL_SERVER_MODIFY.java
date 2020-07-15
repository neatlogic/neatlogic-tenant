package codedriver.module.tenant.auth.label;

import codedriver.framework.auth.core.AuthBase;

public class MAIL_SERVER_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "邮件服务器管理权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "对邮件服务器进行添加、修改和删除";
	}

	@Override
	public String getAuthGroup() {
		return "framework";
	}
}
