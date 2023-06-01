package neatlogic.module.tenant.exception.menu;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class MenuHasChildrenException extends ApiRuntimeException {

    private static final long serialVersionUID = -5716860312997896973L;

    public MenuHasChildrenException(int count) {
        super("当前菜单含有{0}个子菜单，请先移除。", count);
    }

}
