package codedriver.framework.tenant.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.tenant.dao.mapper.MenuMapper;
import codedriver.framework.tenant.dto.MenuVo;

@Service
public class MenuServiceImpl implements MenuService {
	@Autowired
	private MenuMapper menuMapper;

	@Override
	public List<MenuVo> getMenuList(MenuVo vo) {
		List<MenuVo> menuList = menuMapper.getMenuList(vo);
		return menuList;
	}
	
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
		if (menuVo.getRoleIdList() != null) {
			for (String roleName : menuVo.getRoleIdList()) {
				this.menuMapper.insertMenuRole(menuVo.getId(), roleName);
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
