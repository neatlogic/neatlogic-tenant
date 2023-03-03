package neatlogic.module.tenant.exception.api;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class ApiNotAllowedToDeleteException extends ApiRuntimeException {

    private static final long serialVersionUID = 3974228677209813097L;

    public ApiNotAllowedToDeleteException(String token) {
        super("exception.tenant.apinotallowedtodeleteexception", token);
    }
}
