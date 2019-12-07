package codedriver.module.tenant.service;

import java.util.List;

import codedriver.module.tenant.dto.MenuVo;

public interface MenuService {
	public List<MenuVo> getMenuList(MenuVo vo);
	public int checkIsChildern(Long menuId);
	public int saveMenu(MenuVo menuVo);
	public int deleteMenu(Long menuId);

}
