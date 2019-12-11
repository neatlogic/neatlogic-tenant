package codedriver.module.tenant.exception.file;

import codedriver.framework.exception.core.ApiRuntimeException;

public  class BelongNotFoundException extends ApiRuntimeException {
	public BelongNotFoundException(String msg) {
		super(msg);
	}

}
