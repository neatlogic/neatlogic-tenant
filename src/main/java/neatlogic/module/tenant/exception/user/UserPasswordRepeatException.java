package neatlogic.module.tenant.exception.user;

import neatlogic.framework.exception.core.ApiRuntimeException;

import java.io.Serial;

public  class UserPasswordRepeatException extends ApiRuntimeException {

    @Serial
    private static final long serialVersionUID = 3350602030537730388L;

    public UserPasswordRepeatException() {
		super("nmteu.userpasswordrepeatexception.userpasswordrepeatexception");
	}

}
