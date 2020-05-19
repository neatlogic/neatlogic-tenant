package codedriver.module.tenant.service;

import java.util.List;

import codedriver.framework.dto.AuthVo;

import codedriver.framework.dto.RoleVo;

public interface RoleService {
	public List<RoleVo> searchRole(RoleVo roleVo);
}
