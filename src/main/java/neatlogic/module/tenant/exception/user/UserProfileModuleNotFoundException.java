package neatlogic.module.tenant.exception.user;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class UserProfileModuleNotFoundException extends ApiRuntimeException {
    private static final long serialVersionUID = 7033959414053603456L;

    public UserProfileModuleNotFoundException(String msg) {
        super("exception.tenant.userprofilemodulenotfoundexception", msg);
    }

}
