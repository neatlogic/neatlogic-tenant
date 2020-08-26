package codedriver.module.tenant.exception.user;

import codedriver.framework.exception.core.ApiRuntimeException;

public  class UserGetException extends ApiRuntimeException {
	/**
     * 
     */
    private static final long serialVersionUID = 5879583869074093345L;

    public UserGetException(String msg) {
		super(msg);
	}

}
