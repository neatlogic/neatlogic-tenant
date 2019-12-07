package codedriver.module.tenant.service;

import org.springframework.transaction.annotation.Transactional;

import codedriver.framework.dto.UserVo;

public interface UserService {

	@Transactional
	public int saveUser(UserVo userVo);

	@Transactional
	public int deleteUser(String userId);

}
