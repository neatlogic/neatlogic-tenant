package codedriver.framework.tenant.dao.mapper;


import java.util.List;

import org.apache.ibatis.annotations.Param;

import codedriver.framework.tenant.dto.MenuVo;

public interface MenuMapper {

	/*SELECT BLOCK*/
	List<MenuVo> getMenuList(MenuVo vo);
	int checkIsChaildern(Long menuId);
	int getParentIdMaxSort(MenuVo menuVo);
	
	/*INSERT BLOCK*/
	int insertMenu(MenuVo menuVo);
	int insertMenuRole(@Param("menuId")Long menuId, @Param("roleName")String roleName);
	
	/*UPDATE BLOCK*/
	int updateMenu(MenuVo menuVo);
	
	/*DELETE BLOCK*/
	int deleteMenu(Long menuId);
	int deleteMenuRoleByMenuId(Long menuId);

}

