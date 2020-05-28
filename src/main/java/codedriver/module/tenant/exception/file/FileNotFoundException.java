package codedriver.module.tenant.exception.file;

import codedriver.framework.exception.core.ApiRuntimeException;
@SuppressWarnings("serial")
public class FileNotFoundException extends ApiRuntimeException {
	public FileNotFoundException(String uuid) {
		super("附件：" + uuid + "不存在");
	}

}
