package codedriver.module.tenant.exception.user;

import codedriver.framework.exception.core.ApiRuntimeException;

public  class UserPasswordException extends ApiRuntimeException {
	/**
     * 
     */
    private static final long serialVersionUID = 2399885765150585419L;

    public UserPasswordException(String msg) {
		super(msg);
	}

}
