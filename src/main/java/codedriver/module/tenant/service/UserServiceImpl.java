package codedriver.module.tenant.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import codedriver.framework.dto.RoleAuthVo;
import codedriver.framework.dto.UserAuthVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserMapper userMapper;

	@Autowired
	TeamMapper teamMapper;

	@Override
	public int saveUser(UserVo userVo) {
		String userId = userVo.getUserId();
		if (userMapper.getUserByUserId(userId) == null){
			userMapper.insertUser(userVo);
		} else {
			userMapper.updateUser(userVo);
			userMapper.deleteUserRoleByUserId(userId);
			userMapper.deleteUserTeamByUserId(userId);
			//更新密码
			userMapper.updateUserPasswordActive(userId);
			List<Long> idList = userMapper.getLimitUserPasswordIdList(userId);
			if (idList != null && idList.size() > 0){
				userMapper.deleteUserPasswordByLimit(userId, idList);
			}
		}
		userMapper.insertUserPassword(userVo);
		if (userVo.getRoleNameList() != null && userVo.getRoleNameList().size() > 0) {
			for (String roleName : userVo.getRoleNameList()) {
				userMapper.insertUserRole(userId, roleName);
			}
		}
		List<String> teamUuidList = userVo.getTeamUuidList();
		if (teamUuidList != null && teamUuidList.size() > 0) {
			for (String teamUuid : teamUuidList) {
				userMapper.insertUserTeam(userId, teamUuid);
			}

		}

		return 1;
	}

	@Override
	public int addUserAuth(UserVo userVo) {
		List<UserAuthVo> userAuthList = userMapper.searchUserAuthByUserId(userVo.getUserId());
		Set<String> set = new HashSet<>();
		for (UserAuthVo authVo : userAuthList){
			set.add(authVo.getAuth());
		}
		for (UserAuthVo authVo : userVo.getUserAuthList()){
			if (!set.contains(authVo.getAuth())){
				userMapper.insertUserAuth(authVo);
			}
		}
		return 0;
	}

	@Override
	public int coverUserAuth(UserVo userVo) {
		userMapper.deleteUserAuthByUserId(userVo.getUserId());
		if (userVo.getUserAuthList() != null && userVo.getUserAuthList().size() > 0){
			for (UserAuthVo authVo : userVo.getUserAuthList()){
				userMapper.insertUserAuth(authVo);
			}
		}
		return 0;
	}

	@Override
	public int deleteUserAuth(UserVo userVo) {
		userMapper.deleteUserAuth(userVo);
		return 0;
	}

	@Override
	public int deleteUser(String userId) {
		userMapper.deleteUserRoleByUserId(userId);
		userMapper.deleteUserTeamByUserId(userId);
		userMapper.deleteUserByUserId(userId);
		return 1;
	}

	@Override
	public List<UserVo> searchUser(UserVo userVo) {
		if (userVo.getNeedPage()) {
			int rowNum = userMapper.searchUserCount(userVo);
			userVo.setRowNum(rowNum);
			userVo.setPageCount(PageUtil.getPageCount(rowNum, userVo.getPageSize()));
		}
		return userMapper.searchUser(userVo);
	}

	@Override
	public List<UserAuthVo> searchUserAuth(String userId) {
		return userMapper.searchUserAuthByUserId(userId);
	}

	@Override
	public List<RoleAuthVo> searchUserRoleAuth(String userId) {
		return userMapper.searchUserRoleAuthByUserId(userId);
	}

	@Override
	public int updateUserActive(UserVo userVo) {
		userMapper.updateUserActive(userVo);
		return 1;
	}

	@Override
	public UserVo getUserByUserId(String userId) {
		return userMapper.getUserByUserId(userId);
	}

	@Override
	public void updateUserPassword(UserVo userVo) {
		userMapper.updateUserPasswordActive(userVo.getUserId());
		List<Long> idList = userMapper.getLimitUserPasswordIdList(userVo.getUserId());
		userMapper.deleteUserPasswordByLimit(userVo.getUserId(), idList);
		userMapper.insertUserPassword(userVo);
	}
}
