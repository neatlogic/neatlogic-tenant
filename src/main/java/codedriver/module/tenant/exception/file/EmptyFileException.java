package codedriver.module.tenant.exception.file;

import codedriver.framework.exception.core.ApiRuntimeException;

public class EmptyFileException extends ApiRuntimeException {
	public EmptyFileException() {
		super("请唔上传空白附件");
	}

}