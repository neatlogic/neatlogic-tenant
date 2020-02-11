package codedriver.module.tenant.exception.api;

import codedriver.framework.exception.core.ApiRuntimeException;

public class ApiNotAllowedToDeleteException extends ApiRuntimeException {

	private static final long serialVersionUID = 3974228677209813097L;

	public ApiNotAllowedToDeleteException(String token) {
		super("token为：'" + token + "'的接口不允许删除");
	}
}
