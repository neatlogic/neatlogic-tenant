package neatlogic.module.tenant.exception.mailserver;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class MailServerNotFoundException extends ApiRuntimeException {

    private static final long serialVersionUID = 1781086613693485014L;

    public MailServerNotFoundException(String uuid) {
        super("exception.tenant.mailservernotfoundexception", uuid);
    }
}
