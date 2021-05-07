package codedriver.module.tenant.auth.label;

import codedriver.framework.auth.core.AuthBase;

public class MATRIX_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "矩阵管理权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "对矩阵进行添加、修改和删除";
	}

	@Override
	public String getAuthGroup() {
		return "framework";
	}

	@Override
	public Integer getSort() {
		return 12;
	}
}
