package codedriver.module.tenant.exception.user;

import codedriver.framework.exception.core.ApiRuntimeException;

public  class UserCurrentPasswordException extends ApiRuntimeException {
	private static final long serialVersionUID = 1L;

	public UserCurrentPasswordException() {
		super("用户当前密码输入错误,请确认后重新输入");
	}

}
