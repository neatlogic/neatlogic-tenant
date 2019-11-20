package codedriver.framework.tenant.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.dto.UserVo;
import codedriver.framework.tenant.dao.mapper.RoleMapper;
import codedriver.framework.tenant.dto.RoleVo;
import codedriver.framework.tenant.dto.TeamVo;

@Service
public class RoleServiceImpl implements RoleService{

	@Autowired 
	RoleMapper roleMapper;
		
	@Override
	public List<RoleVo> selectAllRole() {
		return roleMapper.selectAllRole();
	}	
	
	@Override
	public List<UserVo> viewUsers(UserVo vo) {
		return roleMapper.viewUsers(vo);
	}
	
	@Override
	public List<TeamVo> selectRoleTeamList(TeamVo teamVo) {
		return roleMapper.selectRoleTeamList(teamVo);
	}
	
	@Override
	public List<RoleVo> getRoleByName(RoleVo roleVo) {
		return roleMapper.getRoleByName(roleVo);
	}

	@Override
	public int saveRole(RoleVo roleVo) {
		if (roleMapper.checkRoleNameExist(roleVo) > 0) {
			roleMapper.updateRole(roleVo);
		}else {
			this.roleMapper.insertRole(roleVo);
		}
		return 1;
	}

	@Override
	public int deleteRole(String name) {
		return roleMapper.deleteRole(name);
	}
	

}
