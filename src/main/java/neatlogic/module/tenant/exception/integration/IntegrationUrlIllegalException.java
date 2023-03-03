package neatlogic.module.tenant.exception.integration;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class IntegrationUrlIllegalException extends ApiRuntimeException {
    private static final long serialVersionUID = -8938446234524797396L;

    public IntegrationUrlIllegalException(String url) {
        super("exception.tenant.integrationurlillegalexception", url);
    }

}
