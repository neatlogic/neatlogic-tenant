package codedriver.framework.tenant.service;

import java.util.List;

import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;

public interface RoleService {
	public List<RoleVo> selectAllRole();
	public List<UserVo> viewUsers(UserVo vo);
	public List<TeamVo> selectRoleTeamList(TeamVo teamVo);
	public List<RoleVo> getRoleByName(RoleVo roleVo);	
	public RoleVo getRoleInfoByName(String name);
	public int saveRole(RoleVo roleVo);
	public int deleteRole(String name);
}
