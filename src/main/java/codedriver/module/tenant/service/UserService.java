package codedriver.module.tenant.service;

import java.util.List;

import codedriver.framework.dto.UserAuthVo;
import org.springframework.transaction.annotation.Transactional;

import codedriver.framework.dto.UserVo;

public interface UserService {

	@Transactional
	public int saveUser(UserVo userVo);

	public int saveUserAuth(UserVo userVo, String action);

	@Transactional
	public int deleteUser(String userId);
	
	@Transactional
	public List<UserVo> searchUser(UserVo userVo);

	public List<UserAuthVo> searchUserAuth(String userId);

	public int updateUserActive(UserVo userVo);

	public UserVo getUserByUserId(String userId);

	public void updateUserPassword(UserVo userVo);

}
