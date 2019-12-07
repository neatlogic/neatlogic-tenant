package codedriver.module.tenant.service;

import java.util.List;

import codedriver.framework.dto.RoleVo;

public interface RoleService {
	public List<RoleVo> selectAllRole();
	public List<RoleVo> getRoleByName(RoleVo roleVo);	
	public RoleVo getRoleInfoByName(String name);
	public int saveRole(RoleVo roleVo);
	public int deleteRole(String name);
}
