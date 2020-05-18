package codedriver.module.tenant.service;

import java.util.List;

import codedriver.framework.dto.AuthVo;

import codedriver.framework.dto.RoleVo;

public interface RoleService {
	public List<RoleVo> searchRole(RoleVo roleVo);

	public int addRoleAuth(RoleVo roleVo);

	public int coverRoleAuth(RoleVo roleVo);

	public int deleteRoleAuth(RoleVo roleVo);

	public RoleVo getRoleByUuid(String uuid);

	public List<AuthVo> getRoleCountByAuth();
}
