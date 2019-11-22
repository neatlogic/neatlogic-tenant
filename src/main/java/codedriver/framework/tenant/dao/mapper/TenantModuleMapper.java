package codedriver.framework.tenant.dao.mapper;

import java.util.List;
import codedriver.framework.dto.ModuleVo;

public interface TenantModuleMapper {
	public List<ModuleVo> getAllActiveModule();
}
