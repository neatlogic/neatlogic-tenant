package codedriver.module.tenant.exception.file;

import codedriver.framework.exception.core.ApiRuntimeException;

public class DirectoryCreateException extends ApiRuntimeException {
	public DirectoryCreateException(String filepath) {
		super("无法创建文件夹：" + filepath);
	}

}
