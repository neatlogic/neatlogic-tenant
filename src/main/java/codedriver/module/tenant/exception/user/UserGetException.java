package codedriver.module.tenant.exception.user;

import codedriver.framework.exception.core.ApiRuntimeException;

public  class UserGetException extends ApiRuntimeException {
	public UserGetException(String msg) {
		super(msg);
	}

}
