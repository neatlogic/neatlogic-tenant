package codedriver.module.tenant.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import codedriver.framework.dto.AuthVo;
import codedriver.framework.dto.RoleAuthVo;
import codedriver.framework.dto.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dto.RoleVo;

@Service
public class RoleServiceImpl implements RoleService {

	private static final String AUTH_DELETE = "delete";
	private static final String AUTH_ADD = "add";
	private static final String AUTH_COVER = "cover";

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
	public List<RoleAuthVo> searchRoleAuth(String roleName) {
		return roleMapper.searchRoleAuthByRoleName(roleName);
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
	public int addRoleAuth(RoleVo roleVo) {
		List<RoleAuthVo> roleAuthVoList = roleMapper.searchRoleAuthByRoleName(roleVo.getName());
		Set<String> authSet = new HashSet<>();
		for (RoleAuthVo authVo : roleAuthVoList){
			authSet.add(authVo.getAuth());
		}
		for (RoleAuthVo roleAuth : roleVo.getRoleAuthList()){
			if (!authSet.contains(roleAuth.getAuth())){
				roleMapper.insertRoleAuth(roleAuth);
			}
		}
		return 0;
	}

	@Override
	public int coverRoleAuth(RoleVo roleVo) {
		roleMapper.deleteRoleAuthByRoleName(roleVo.getName());
		List<RoleAuthVo> roleAuthVoList = roleVo.getRoleAuthList();
		if (roleAuthVoList != null && roleAuthVoList.size() > 0){
			for (RoleAuthVo roleAuthVo : roleAuthVoList){
				roleMapper.insertRoleAuth(roleAuthVo);
			}
		}
		return 0;
	}

	@Override
	public int deleteRoleAuth(RoleVo roleVo) {
		roleMapper.deleteRoleAuth(roleVo);
		return 0;
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

	@Override
	public List<AuthVo> getRoleCountByAuth() {
		return roleMapper.getRoleCountByAuth();
	}
}
