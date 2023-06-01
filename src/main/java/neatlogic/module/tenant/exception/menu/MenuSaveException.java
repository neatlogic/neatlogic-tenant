package neatlogic.module.tenant.exception.menu;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class MenuSaveException extends ApiRuntimeException {
    private static final long serialVersionUID = -6616423038844483065L;

    public MenuSaveException() {
        super("菜单id不合法，不能与父菜单id相同");
    }

}
