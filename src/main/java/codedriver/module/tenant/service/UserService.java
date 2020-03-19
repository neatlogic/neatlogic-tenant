package codedriver.module.tenant.service;

import java.util.List;

import codedriver.framework.dto.AuthVo;
import codedriver.framework.dto.RoleAuthVo;
import codedriver.framework.dto.UserAuthVo;
import org.springframework.transaction.annotation.Transactional;

import codedriver.framework.dto.UserVo;

public interface UserService {

	@Transactional
	public int saveUser(UserVo userVo);

	public int addUserAuth(UserVo userVo);

	public int coverUserAuth(UserVo userVo);

	public int deleteUserAuth(UserVo userVo);

	@Transactional
	public int deleteUser(String userId);
	
	@Transactional
	public List<UserVo> searchUser(UserVo userVo);

	public List<UserAuthVo> searchUserAuth(String userId);

	public List<RoleAuthVo> searchUserRoleAuth(String userId);

	public int updateUserActive(UserVo userVo);

	public UserVo getUserByUserId(String userId);

	public void updateUserPassword(UserVo userVo);

	public List<AuthVo> getUserCountByAuth();

	public void saveUserAuth(List<UserAuthVo> userAuthList, String auth);

}
