package codedriver.framework.tenant.service;

import java.util.List;
import java.util.Map;

import codedriver.framework.dto.UserVo;

public interface UserAccountService {
	
	public Map<String, Object> getUserList(UserVo userVo);
	
	public List<UserVo> getUserListByRole(UserVo userVo);
	
	public int saveUser(UserVo userVo);
	
	public UserVo getUserDetailByUserId(String userId);
	
	public int deleteUser(String userId);
}
