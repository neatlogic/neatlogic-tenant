package codedriver.framework.tenant.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;

import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.tenant.dao.mapper.RoleMapper;

@Service
public class RoleServiceImpl implements RoleService{

	@Autowired 
	RoleMapper roleMapper;
		
	
	@Override
	public RoleVo getRoleInfoByName(String name) {
		return roleMapper.getRoleInfoByName(name);
	}

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

//	@Override
//	public int updateTeamRole(List<Map<String, Object>> teamList, String roleName, String flag) {
//		this.roleMapper.deleteTeamRoleByRoleName(roleName);
//		int count = 0;
//		if (teamList.size() > 0) {
//			count = roleMapper.insertTeamRole(teamList);
//			for (int i = 0; i < teamList.size(); i++) {
//				Map<String, Object> map = teamList.get(i);
//				if ("1".equals(flag) && "true".equals(map.get("isParent")) && "false".equals(map.get("isOpen"))) {
//					int c = this.roleMapper.insertTeamChildrenRole(Long.parseLong(map.get("teamId").toString()), roleName);
//					count = count + c;
//				}
//			}
//		}
//		return count;
//	}
	
}
