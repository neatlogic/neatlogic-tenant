package neatlogic.module.tenant.exception.mailserver;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class MailServerNameRepeatException extends ApiRuntimeException {

    private static final long serialVersionUID = -2768806372998827228L;

    public MailServerNameRepeatException(String name) {
        super("邮件服务器名称：“{0}”已存在", name);
    }
}
