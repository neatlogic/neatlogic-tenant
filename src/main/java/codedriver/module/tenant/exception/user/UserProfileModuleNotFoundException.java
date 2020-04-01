package codedriver.module.tenant.exception.user;

import codedriver.framework.exception.core.ApiRuntimeException;

public  class UserProfileModuleNotFoundException extends ApiRuntimeException {
	private static final long serialVersionUID = 7033959414053603456L;

	public UserProfileModuleNotFoundException(String msg) {
		super("用户个性化 '"+msg+"' 不存在");
	}

}
