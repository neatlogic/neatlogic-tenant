package codedriver.module.tenant.service;

import java.util.List;

import codedriver.framework.dto.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dto.RoleVo;

@Service
public class RoleServiceImpl implements RoleService {

	@Autowired
	RoleMapper roleMapper;



	@Override
	public List<RoleVo> searchRole(RoleVo roleVo){
		if(roleVo.getNeedPage()) {
			int rowNum = roleMapper.searchRoleCount(roleVo);
			roleVo.setPageCount(PageUtil.getPageCount(rowNum, roleVo.getPageSize()));
			roleVo.setRowNum(rowNum);
		}
		return roleMapper.searchRole(roleVo);
	}
	@Override
	public int saveRole(RoleVo roleVo) {
		if (roleMapper.getRoleByRoleName(roleVo.getName()) != null) {
			roleMapper.updateRole(roleVo);
		} else {
			roleMapper.insertRole(roleVo);
		}
		return 1;
	}

	@Override
	public int deleteRoleByRoleName(String name) {
		roleMapper.deleteMenuRoleByRoleName(name);
		roleMapper.deleteTeamRoleByRoleName(name);
		roleMapper.deleteUserRoleByRoleName(name);
		roleMapper.deleteRoleByRoleName(name);
		return 1;
	}

	@Override
	public int saveRoleUser(String roleName, String userId) {
		UserVo userVo = new UserVo();
		userVo.setUserId(userId);
		userVo.setRoleName(roleName);
		roleMapper.insertRoleUser(userVo);
		return 1;
	}

	@Override
	public RoleVo getRoleByRoleName(String roleName) {
		RoleVo roleVo = roleMapper.getRoleByRoleName(roleName);
		int userCount = roleMapper.searchRoleUserCountByRoleName(roleName);
		roleVo.setUserCount(userCount);
		return roleVo;
	}
}
