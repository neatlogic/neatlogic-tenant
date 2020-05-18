package codedriver.module.tenant.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dto.AuthVo;
import codedriver.framework.dto.RoleAuthVo;
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

//	@Override
//	public List<RoleAuthVo> searchRoleAuth(String roleName) {
//		return roleMapper.searchRoleAuthByRoleName(roleName);
//	}

	@Override
	public int addRoleAuth(RoleVo roleVo) {
		List<RoleAuthVo> roleAuthVoList = roleMapper.searchRoleAuthByRoleUuid(roleVo.getUuid());
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
		roleMapper.deleteRoleAuthByRoleUuid(roleVo.getUuid());
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

	@Override
	public void saveAuthRole(List<RoleAuthVo> roleAuthList, String auth) {
		roleMapper.deleteRoleAuthByAuth(auth);
		if (roleAuthList.size() > 0){
			for (RoleAuthVo roleAuthVo : roleAuthList){
				roleMapper.insertRoleAuth(roleAuthVo);
			}
		}
	}
}
