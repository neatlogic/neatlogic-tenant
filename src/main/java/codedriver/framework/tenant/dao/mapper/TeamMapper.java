package codedriver.framework.tenant.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import codedriver.framework.dto.TeamUserVo;
import codedriver.framework.dto.TeamVo;

public interface TeamMapper {
	
	public List<TeamVo> selectTeamList(TeamVo teamVo);
	
	public List<TeamUserVo> getTeamUserByUserIdTeamIds(@Param("userId") String userId,@Param("teamList") List<Long> teamList);
	
	public List<TeamVo> searchTeamByName(TeamVo teamVo);
	
	public int insertUserTeam(TeamUserVo teamUserVo);
	
	public int insertTeamRole(List<TeamVo> list);
	
	public int insertTeamChildrenRole(@Param("parentId") Long parentId, @Param("roleName") String roleName);
	
	public int deleteTeamRoleByRoleName(String roleName);
}
