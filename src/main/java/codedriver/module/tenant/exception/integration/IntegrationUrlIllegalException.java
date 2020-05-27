package codedriver.module.tenant.exception.integration;

import codedriver.framework.exception.core.ApiRuntimeException;

public class IntegrationUrlIllegalException extends ApiRuntimeException {
	private static final long serialVersionUID = -8938446234524797396L;

	public IntegrationUrlIllegalException(String url) {
		super("地址：" + url + "可能是集成调用地址，不允许配置");
	}

}
