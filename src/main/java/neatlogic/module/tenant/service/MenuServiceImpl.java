package neatlogic.module.tenant.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import neatlogic.module.tenant.dao.mapper.MenuMapper;
import neatlogic.module.tenant.dto.MenuVo;

@Service
@Transactional
public class MenuServiceImpl implements MenuService {
	@Autowired
	private MenuMapper menuMapper;
	
	@Override
	public int checkIsChildern(Long menuId) {
		return menuMapper.checkIsChaildern(menuId);
	}
	
	@Override
	public int saveMenu(MenuVo menuVo) {
		if (menuVo.getId() == null) {
			menuVo.setSort(this.menuMapper.getParentIdMaxSort(menuVo));
			menuMapper.insertMenu(menuVo);
		} else {
			menuMapper.deleteMenuRoleByMenuId(menuVo.getId());
			menuMapper.updateMenu(menuVo);
		}
		if (menuVo.getRoleUuidList() != null) {
			for (String roleUuid : menuVo.getRoleUuidList()) {
				this.menuMapper.insertMenuRole(menuVo.getId(), roleUuid);
			}
		}
		return 1;
	}
	
	@Override
	public int deleteMenu(Long menuId) {
		menuMapper.deleteMenuRoleByMenuId(menuId);
		menuMapper.deleteMenu(menuId);
		return 1;
	}

	
}
