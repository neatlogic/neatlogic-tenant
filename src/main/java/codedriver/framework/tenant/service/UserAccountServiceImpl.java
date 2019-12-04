package codedriver.framework.tenant.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dto.TeamUserVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.tenant.dao.mapper.TeamMapper;
import codedriver.framework.tenant.dao.mapper.UserAccountMapper;

@Service
@Transactional
public class UserAccountServiceImpl implements UserAccountService{
	
	@Autowired
	UserAccountMapper userAccountMapper;
	
	@Autowired
	TeamMapper teamMapper;

	@Override
	public Map<String, Object> getUserList(UserVo userVo) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<UserVo> userList = userAccountMapper.getUserList(userVo);
		int rownum = userAccountMapper.getUserListCount(userVo);
		int pageCount = PageUtil.getPageCount(rownum, userVo.getPageSize());
		resultMap.put("totalCount", rownum);
		resultMap.put("pageCount", pageCount);
		resultMap.put("resultList", userList);
		return resultMap;
	}
	
	@Override	
	public List<UserVo> getUserListByRole(UserVo userVo) {
		return userAccountMapper.getUserListByRole(userVo);
	}

	@Override
	public UserVo getUserDetailByUserId(String userId) {
		return userAccountMapper.getUserDetailByUserId(userId);
	}
	
	@Override
	public int saveUser(UserVo userVo) {
		String userId = userVo.getUserId();	
		if (userAccountMapper.checkUserExists(userVo) == null) {
			userAccountMapper.insertUser(userVo);
			userAccountMapper.insertUserInfo(userVo);
		}else {
			userAccountMapper.updateUser(userVo);
			userAccountMapper.updateUserInfo(userVo);
			userAccountMapper.deleteUserRoleByUserId(userId);
			userAccountMapper.deleteTeamUser(userId, null);
		}

			if (userVo.getRoleList() != null && userVo.getRoleList().size() > 0) {
				for (String roleName : userVo.getRoleList()) {
					userAccountMapper.insertUserRole(userId, roleName);
				}
			}
			List<TeamUserVo> teamUserNewList = new ArrayList<>();
			List<String> teamUuidList = userVo.getTeamUuidList();		
			if(teamUuidList != null && teamUuidList.size() > 0) {
				List<TeamUserVo> teamUserOldList = teamMapper.getTeamUserByUserIdTeamIds(userId,teamUuidList);
				if(teamUserOldList != null && teamUserOldList.size()>0) {
					teamUserNewList.addAll(teamUserOldList);
					for(TeamUserVo teamUserOld : teamUserOldList) {
						teamUuidList.remove(teamUserOld.getTeamUuid());
					}
				}
				for(String teamId : teamUuidList) {
					TeamUserVo teamUserNew = new TeamUserVo();
					teamUserNew.setUserId(userId);
					teamUserNew.setTeamUuid(teamId);
					teamUserNewList.add(teamUserNew);
				}
			}
			for(TeamUserVo teamUserNew : teamUserNewList) {
				teamMapper.insertUserTeam(teamUserNew);
			}
		
		return 1;
	}
	
	@Override
	public int deleteUser(String userId) {
		userAccountMapper.deleteUser(userId);
		userAccountMapper.deleteUserInfo(userId);
		userAccountMapper.deleteUserRoleByUserId(userId);
		userAccountMapper.deleteTeamUser(userId, null);
		return 1;
	}

	@Override
	public int batchUpdateUser(Integer isActive, List<String> userIdList) {
		if(isActive==null) {
			userAccountMapper.batchDeleteUser(userIdList);
			userAccountMapper.batchDeleteUserRoleByUserIdList(userIdList);
			userAccountMapper.batchDeleteTeamUserByUserIdList(userIdList);
			userAccountMapper.batchDeleteUserInfoByUserIdList(userIdList);
		}else {
			userAccountMapper.batchUpdateUserStatus(isActive, userIdList);
		}
		return 0;
	}
	
}
