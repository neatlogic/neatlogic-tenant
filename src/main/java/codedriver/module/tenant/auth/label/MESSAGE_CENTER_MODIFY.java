package codedriver.module.tenant.auth.label;

import codedriver.framework.auth.core.AuthBase;

public class MESSAGE_CENTER_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "消息中心管理权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "对消息进行添加、删除、修改";
	}

	@Override
	public String getAuthGroup() {
		return "framework";
	}
}
