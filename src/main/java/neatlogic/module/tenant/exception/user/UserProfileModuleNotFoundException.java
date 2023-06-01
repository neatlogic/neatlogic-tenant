package neatlogic.module.tenant.exception.user;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class UserProfileModuleNotFoundException extends ApiRuntimeException {
    private static final long serialVersionUID = 7033959414053603456L;

    public UserProfileModuleNotFoundException(String msg) {
        super("用户个性化 “{0}” 不存在", msg);
    }

}
