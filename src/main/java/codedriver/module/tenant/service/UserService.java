package codedriver.module.tenant.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import codedriver.framework.dto.UserVo;

public interface UserService {

	@Transactional
	public int saveUser(UserVo userVo);

	@Transactional
	public int deleteUser(String userId);
	
	@Transactional
	public List<UserVo> searchUser(UserVo userVo);

}
