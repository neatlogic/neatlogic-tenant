package codedriver.module.tenant.exception.file;

import codedriver.framework.exception.core.ApiRuntimeException;

public class FileAccessDeniedException extends ApiRuntimeException {
	public FileAccessDeniedException(String filename) {
		super("您没有权限下载文件：" + filename);
	}

}
