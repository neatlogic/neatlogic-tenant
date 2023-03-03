package neatlogic.module.tenant.exception.menu;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class MenuHasChildrenException extends ApiRuntimeException {

    private static final long serialVersionUID = -5716860312997896973L;

    public MenuHasChildrenException(int count) {
        super("exception.tenant.menudeleteexception", count);
    }

}
