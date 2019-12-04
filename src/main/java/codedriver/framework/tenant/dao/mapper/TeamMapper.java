package codedriver.framework.tenant.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import codedriver.framework.dto.TeamUserVo;
import codedriver.framework.dto.TeamVo;

public interface TeamMapper {

	public List<TeamUserVo> getTeamUserByUserIdTeamIds(@Param("userId") String userId,@Param("teamUuidList") List<String> teamUuidList);

	public List<TeamVo> selectTeamList(TeamVo teamVo);
	
	public List<TeamVo> searchTeamByName(TeamVo teamVo);
	
	public TeamVo getTeamByUuid(String teamUuid);
	
	public TeamVo selectTeamById(String teamId);
	
	public List<TeamVo> getTeamListByParentId(@Param("parentId") String parentId);
	
	public List<TeamVo> getTeamsByLeftRight(TeamVo teamVo);
	
	public List<TeamVo> getTreePathByTeamUuid(TeamVo teamVo);
	
	public int insertTeam(TeamVo teamVo);
	
	public int insertUserTeam(TeamUserVo teamUserVo);
	
	public int insertTeamRole(List<TeamVo> list);
	
	public int insertTeamChildrenRole(@Param("parentId") Long parentId, @Param("roleName") String roleName);	
	
	public int updateTeamRightForInsert(Integer right);

	public int updateTeamLeftForInsert(Integer left);
	
	public int updateTeam(TeamVo teamVo);
	
	public int updateTeamLeftForDelete(TeamVo teamVo);

	public int updateTeamRightForDelete(TeamVo teamVo);
	
	public int updateTeamLeftRightCode(@Param("uuid") String uuid, @Param("lft") Integer lft, @Param("rht") Integer rht);
	
	public int updateTeamParentId(@Param("uuid") String uuid, @Param("parentId") String parentId);
	
	public int updateTeamLeftByLeft(@Param("diff") int diff, @Param("minLeft") int minLeft, @Param("maxLeft") int maxLeft, @Param("teamUuidList") List<String> teamUuidList);

	public int updateTeamRightByRight(@Param("diff") int diff, @Param("minRight") int minRight, @Param("maxRight") int maxRight, @Param("teamUuidList") List<String> teamUuidList);
	
	public int updateTeamLeftRight(@Param("diff") int diff, @Param("teamUuidList") List<String> teamUuidList);
		
	public int deleteTeam(String teamUuid);
	
	public int deleteTeamRoleByRoleName(String roleName);
}
