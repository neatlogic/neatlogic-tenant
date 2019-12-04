package codedriver.framework.tenant.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import codedriver.framework.dto.TeamUserVo;
import codedriver.framework.dto.TeamVo;

public interface TeamMapper {

	public List<TeamUserVo> getTeamUserByUserIdTeamIds(@Param("userId") String userId,@Param("teamUuidList") List<String> teamUuidList);

	public int insertUserTeam(TeamUserVo teamUserVo);

	public List<TeamVo> selectTeamList(TeamVo teamVo);
	
	public List<TeamVo> searchTeamByName(TeamVo teamVo);
	
	public int insertTeamRole(List<TeamVo> list);
	
	public int insertTeamChildrenRole(@Param("parentId") Long parentId, @Param("roleName") String roleName);
	
	public int deleteTeamRoleByRoleName(String roleName);	
	
	public TeamVo getTeamByUuid(String teamUuid);
	
	public TeamVo selectTeamById(String teamId);
	
	public List<TeamVo> getTeamListByParentId(@Param("parentId") String parentId);
	
	public int updateTeamRightForInsert(Integer right);

	public int updateTeamLeftForInsert(Integer left);
	
	public int updateTeam(TeamVo teamVo);
	
	public int updateTeamLeftForDelete(TeamVo teamVo);

	public int updateTeamRightForDelete(TeamVo teamVo);
	
	public int updateTeamLeftRightCode(@Param("uuid") String uuid, @Param("lft") Integer lft, @Param("rht") Integer rht);
	
	public int insertTeam(TeamVo teamVo);
	
	public int deleteTeam(String teamUuid);
}
