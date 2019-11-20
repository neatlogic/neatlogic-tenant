package codedriver.framework.tenant.api.menu;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.dto.MenuVo;
import codedriver.framework.tenant.service.MenuService;

@Service
@AuthAction(name="SYSTEM_MENU_EDIT")
public class MenuQueryApi extends ApiComponentBase{

	@Autowired
	private MenuService menuService;
	
	@Override
	public String getToken() {
		return "menuQueryApi";
	}

	@Override
	public String getName() {
		return "查询菜单接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "id", type = "int", desc = "菜单id") })
	@Output({})
	@Description(desc = "查询菜单接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		//List<MenuVo> menuList = menuService.getAllMenu();
		JSONObject json = new JSONObject();
		MenuVo vo = new MenuVo();
		vo.setId(jsonObj.getLong("id"));
		vo.setParentId(jsonObj.getLong("parentId"));
		vo.setModule(jsonObj.getString("module"));
		List<MenuVo> menuList = menuService.getMenuList(vo);
		json.put("menuList",menuList);
		return json;
	}
}
