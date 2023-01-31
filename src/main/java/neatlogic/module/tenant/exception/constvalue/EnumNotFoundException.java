package neatlogic.module.tenant.exception.constvalue;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class EnumNotFoundException extends ApiRuntimeException {
	private static final long serialVersionUID = 7737513517395415836L;

	public EnumNotFoundException(String enumClass) {
		super("枚举：" + enumClass + "不存在");
	}

}
