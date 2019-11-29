package codedriver.framework.tenant.api.menu;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.param.AuthParamType;
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
		@Param(name = "id", type = AuthParamType.LONG, desc = "菜单id" ,isRequired = false),
		@Param(name = "parentId", type = AuthParamType.LONG, desc = "菜单父节点id" ,isRequired = false),
		@Param(name = "type", type = AuthParamType.LONG, desc = "默认0，0:返回全部菜单，1:根据角色返回" ,isRequired = false)
		})
	@Output({
		@Param(name = "id", type = AuthParamType.LONG, desc = "菜单id"),
		@Param(name = "parentId", type = AuthParamType.LONG, desc = "父节点id") ,
		@Param(name = "name", type = AuthParamType.STRING, desc = "菜单名称"),
		@Param(name = "url", type = AuthParamType.STRING, desc = "菜单url"),
		@Param(name = "description", type = AuthParamType.STRING, desc = "菜单描述"),
		@Param(name = "module", type = AuthParamType.STRING, desc = "模块名"),
		@Param(name = "isActive", type = AuthParamType.LONG, desc = "是否启用，0:正常，1:禁用"),
		@Param(name = "isAuto", type = AuthParamType.LONG, desc = "是否自动打开，0:否，1:是"),
		@Param(name = "openMode", type = AuthParamType.STRING, desc = "打开页面方式，tab:打开新tab页面   blank:打开新标签页"),
		@Param(name = "icon", type = AuthParamType.STRING, desc = "目录对应的图标class"), 
		@Param(name = "roleName", type = AuthParamType.STRING, desc = "角色") 
	})
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
