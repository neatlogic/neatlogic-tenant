package codedriver.module.tenant.service;

import java.util.List;

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
		if (userMapper.getUserByUserId(userId) == null) {
			userMapper.insertUser(userVo);
		} else {
			userMapper.updateUser(userVo);
			userMapper.deleteUserRoleByUserId(userId);
			userMapper.deleteUserTeamByUserId(userId);
		}

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
	public int updateUserActive(UserVo userVo) {
		userMapper.updateUserActive(userVo);
		return 1;
	}
}
