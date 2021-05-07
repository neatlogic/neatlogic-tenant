package codedriver.module.tenant.auth.label;

import codedriver.framework.auth.core.AuthBase;

public class SYSTEM_NOTICE_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "系统公告管理权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "对系统公告进行添加、修改和删除";
	}

	@Override
	public String getAuthGroup() {
		return "framework";
	}

	@Override
	public Integer getSort() {
		return 17;
	}
}
