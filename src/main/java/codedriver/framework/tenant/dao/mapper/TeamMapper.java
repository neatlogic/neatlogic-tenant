package codedriver.framework.tenant.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import codedriver.framework.dto.TeamUserVo;

public interface TeamMapper {

	public List<TeamUserVo> getTeamUserByUserIdTeamIds(@Param("userId") String userId,@Param("teamUuidList") List<String> teamUuidList);

	public int insertUserTeam(TeamUserVo teamUserVo);

	
	
}
