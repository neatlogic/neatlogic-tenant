package codedriver.framework.tenant.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import codedriver.framework.dto.UserVo;
import codedriver.framework.tenant.dto.RoleVo;
import codedriver.framework.tenant.dto.TeamVo;

public interface RoleMapper {
	public List<RoleVo> selectAllRole();
	
	public List<RoleVo> getRoleByName(RoleVo roleVo);
	
	public List<UserVo> viewUsers(UserVo vo);
	
	public List<TeamVo> selectRoleTeamList(TeamVo vo);
		
	public int insertRole(RoleVo roleVo);
	
	public int updateRole(RoleVo roleVo);
	
	public int checkRoleNameExist(RoleVo roleVo);
	
	public int deleteRole(@Param("name") String name);
}
