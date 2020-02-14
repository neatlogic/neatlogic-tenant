package codedriver.module.tenant.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import codedriver.framework.dto.RoleVo;

public interface RoleService {
	public List<RoleVo> searchRole(RoleVo roleVo);

	public int searchRoleCount(RoleVo roleVo);

	@Transactional
	public int saveRole(RoleVo roleVo);

	@Transactional
	public int deleteRoleByRoleName(String name);

	public int saveRoleUser(String roleName, String userId);

	public RoleVo getRoleByRoleName(String roleName);
}
