package neatlogic.module.tenant.exception.user;

import neatlogic.framework.exception.core.ApiRuntimeException;

public  class UserCurrentPasswordException extends ApiRuntimeException {
	private static final long serialVersionUID = 1L;

	public UserCurrentPasswordException() {
		super("exception.tenant.usercurrentpasswordexception");
	}

}
