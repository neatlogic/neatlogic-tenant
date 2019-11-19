package codedriver.framework.tenant.api.menu;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.AuthAction;
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
		return "查询租户菜单接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

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
