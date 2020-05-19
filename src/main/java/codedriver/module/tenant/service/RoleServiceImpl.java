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

}
