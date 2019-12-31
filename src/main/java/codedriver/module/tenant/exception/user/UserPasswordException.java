package codedriver.module.tenant.exception.user;

import codedriver.framework.exception.core.ApiRuntimeException;

public  class UserPasswordException extends ApiRuntimeException {
	public UserPasswordException(String msg) {
		super(msg);
	}

}
