package codedriver.module.tenant.service;

import java.util.List;

import codedriver.framework.dto.RoleAuthVo;
import org.springframework.transaction.annotation.Transactional;

import codedriver.framework.dto.RoleVo;

public interface RoleService {
	public List<RoleVo> searchRole(RoleVo roleVo);

	public List<RoleAuthVo> searchRoleAuth(String roleName);

	@Transactional
	public int saveRole(RoleVo roleVo);

	public int addRoleAuth(RoleVo roleVo);

	public int coverRoleAuth(RoleVo roleVo);

	public int deleteRoleAuth(RoleVo roleVo);

	@Transactional
	public int deleteRoleByRoleName(String name);

	public int saveRoleUser(String roleName, String userId);

	public RoleVo getRoleByRoleName(String roleName);
}
