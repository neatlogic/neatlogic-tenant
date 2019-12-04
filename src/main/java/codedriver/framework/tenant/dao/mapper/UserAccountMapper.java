package codedriver.framework.tenant.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import codedriver.framework.dto.UserVo;

public interface UserAccountMapper {
	
	public List<UserVo> getUserList(UserVo userVo);
	
	public List<UserVo> getUserListByRole(UserVo vo);
	
	public UserVo getUserDetailByUserId(@Param("userId") String userId);
	
	public int getUserListCount(UserVo userVo);
	
	public UserVo checkUserExists(UserVo userVo);
	
	public int insertUser(UserVo userVo);
	
	public int insertUserInfo(UserVo userVo);
	
	public int insertUserRole(@Param("userId") String userId, @Param("roleName") String roleName);
	
	public int updateUser(UserVo userVo);
	
	public int updateUserInfo(UserVo userVo);
	
	public int deleteUserRoleByUserId(String userId);
	
	public int deleteTeamUser(@Param("userId") String userId, @Param("teamUuid") Long teamUuid);
	
	public int deleteUser(String userId);
	
	public int deleteUserInfo(String userId);
	
	public int batchDeleteUser(@Param("userIdList") List<String> userIdList);

	public int batchUpdateUserStatus(@Param("isActive") Integer isActive,@Param("userIdList") List<String> userIdList);
	
	public int batchDeleteUserRoleByUserIdList(@Param("userIdList") List<String> userIdList);
	
	public int batchDeleteTeamUserByUserIdList(@Param("userIdList") List<String> userIdList);
	
	public int batchDeleteUserInfoByUserIdList(@Param("userIdList") List<String> userIdList);

}
