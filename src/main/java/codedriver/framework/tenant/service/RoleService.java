package codedriver.framework.tenant.service;

import java.util.List;

import codedriver.framework.dto.UserVo;
import codedriver.framework.tenant.dto.RoleVo;
import codedriver.framework.tenant.dto.TeamVo;

public interface RoleService {
	public List<RoleVo> selectAllRole();
	public List<UserVo> viewUsers(UserVo vo);
	public List<TeamVo> selectRoleTeamList(TeamVo teamVo);
	public List<RoleVo> getRoleByName(RoleVo roleVo);	
	public int saveRole(RoleVo roleVo);
	public int deleteRole(String name);
}
