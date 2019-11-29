package codedriver.framework.exception;

import codedriver.framework.exception.core.IApiExceptionMessage;

public  class MenuExceptionMessage implements IApiExceptionMessage {
	private String errorCode = "01";
	private String error = "菜单功能-";
	
	public MenuExceptionMessage(IApiExceptionMessage exception){
		this.errorCode += exception.getErrorCode();
		this.error += exception.getError();
	}
	
	@Override
	public final String getErrorCode() {
		return errorCode;
	}

	@Override
	public String getError() {
		return error;
	}

}
