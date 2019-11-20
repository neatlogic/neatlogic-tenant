package codedriver.framework.tenant.dto;

import java.io.Serializable;
import java.util.List;

import codedriver.framework.common.dto.BasePageVo;

public class RoleVo extends BasePageVo implements Serializable{

	private static final long serialVersionUID = -8007028390813552667L;

	public static final String USER_DEFAULT_ROLE = "R_SYSTEM_USER";

	private String roleName;
	private String roleDesc;
	
	public RoleVo(String roleName) {
		this.roleName = roleName;
	}

	public RoleVo() {
		this.setPageSize(20);
	}

	public RoleVo(String roleName, String roleDesc) {
		this.roleName = roleName;
		this.roleDesc = roleDesc;
	}

	public String getRoleName() {
		return this.roleName == null ? this.roleName : roleName.toUpperCase();
	}

	public void setRoleName(String roleName) {
		if(roleName != null) {
			this.roleName = roleName.toUpperCase();
		}
	}

	public String getRoleDesc() {
		return roleDesc;
	}

	public void setRoleDesc(String roleDesc) {
		this.roleDesc = roleDesc;
	}

}

