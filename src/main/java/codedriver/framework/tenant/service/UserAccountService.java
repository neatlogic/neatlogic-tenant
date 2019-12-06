package codedriver.framework.tenant.service;

import java.util.List;

import codedriver.framework.dto.UserVo;

public interface UserAccountService {
	
	public List<UserVo> getUserList(UserVo userVo);
	
	public List<UserVo> getUserListByRole(UserVo userVo);
	
	public int saveUser(UserVo userVo);
	
	public UserVo getUserDetailByUserId(String userId);
	
	public int deleteUser(String userId);
	
	public int batchUpdateUser(Integer isActive,List<String> userIdList);
}
