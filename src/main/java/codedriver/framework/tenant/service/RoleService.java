package codedriver.framework.tenant.service;

import java.util.List;
import java.util.Map;

import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;

public interface RoleService {
	public List<RoleVo> selectAllRole();
	public List<RoleVo> getRoleByName(RoleVo roleVo);	
	public RoleVo getRoleInfoByName(String name);
	public int saveRole(RoleVo roleVo);
	public int deleteRole(String name);
	//public int updateTeamRole(List<Map<String,Object>> teamList,String roleName,String flag);
}
