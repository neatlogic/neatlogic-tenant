package neatlogic.module.tenant.exception.team;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class TeamMoveException extends ApiRuntimeException {

	private static final long serialVersionUID = 5963454717003929732L;
	
	public TeamMoveException(String msg) {
		super(msg);
	};

}
