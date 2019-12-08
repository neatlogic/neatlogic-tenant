package codedriver.module.tenant.service;

import java.util.List;

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

}
