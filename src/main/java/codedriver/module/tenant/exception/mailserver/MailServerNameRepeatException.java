package codedriver.module.tenant.exception.mailserver;

import codedriver.framework.exception.core.ApiRuntimeException;

public class MailServerNameRepeatException extends ApiRuntimeException {

	private static final long serialVersionUID = -2768806372998827228L;

	public MailServerNameRepeatException(String name) {
		super("邮件服务器名称：'" + name + "'已存在");
	}
}
