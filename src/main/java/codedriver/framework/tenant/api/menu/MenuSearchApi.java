package codedriver.framework.tenant.api.menu;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
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
public class MenuSearchApi extends ApiComponentBase{

	@Autowired
	private MenuService menuService;
	
	@Override
	public String getToken() {
		return "menu/search";
	}

	@Override
	public String getName() {
		return "查询菜单接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ 
		@Param(name = "id", type = "int", desc = "菜单id" ,isRequired="false"),
		@Param(name = "parentId", type = "int", desc = "菜单父节点id" ,isRequired="false"),
		@Param(name = "type", type = "int", desc = "默认0，0:返回全部菜单，1:根据角色返回" ,isRequired="false")
		})
	@Output({})
	@Description(desc = "查询菜单接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject json = new JSONObject();
		List<MenuVo> menuList = new ArrayList<MenuVo>();
		List<String> roleNameList = new ArrayList<String>();
		//如果是根据角色返回对应菜单
		if(jsonObj.containsKey("type") && jsonObj.getIntValue("type") == 1) {
			UserContext userContext = UserContext.get();
			roleNameList = userContext.getRoleNameList();
		}
		menuList = menuService.getMenuList(new MenuVo(jsonObj.getLong("id"),jsonObj.getLong("parentId"),roleNameList));
		json.put("menuList",menuList);
		return json;
	}
}
