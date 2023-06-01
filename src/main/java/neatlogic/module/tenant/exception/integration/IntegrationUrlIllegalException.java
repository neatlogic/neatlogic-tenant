package neatlogic.module.tenant.exception.integration;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class IntegrationUrlIllegalException extends ApiRuntimeException {
    private static final long serialVersionUID = -8938446234524797396L;

    public IntegrationUrlIllegalException(String url) {
        super("地址：{0}可能是集成调用地址，不允许配置", url);
    }

}
