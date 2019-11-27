package codedriver.framework.tenant.api.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.dto.MenuVo;
import codedriver.framework.tenant.service.MenuService;

@Service
public class MenuSaveApi extends ApiComponentBase{

	@Autowired
	private MenuService menuService;
	
	@Override
	public String getToken() {
		return "menu/menuSaveApi";
	}

	@Override
	public String getName() {
		return "保存租户菜单接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ 
		@Param(name = "id", type = "long", desc = "菜单id",isRequired="false"),
		@Param(name = "parentId", type = "long", desc = "菜单id",isRequired="true") ,
		@Param(name = "name", type = "string", desc = "菜单id",isRequired="true"),
		@Param(name = "url", type = "string", desc = "菜单id",isRequired="true"),
		@Param(name = "description", type = "string", desc = "菜单id",isRequired="true"),
		@Param(name = "module", type = "string", desc = "菜单id",isRequired="true"),
		@Param(name = "isActive", type = "int", desc = "菜单id",isRequired="true"),
		@Param(name = "isAuto", type = "int", desc = "菜单id",isRequired="true"),
		@Param(name = "openMode", type = "string", desc = "菜单id",isRequired="true"),
		@Param(name = "icon", type = "string", desc = "菜单id",isRequired="true") 
		})
	@Output({
		@Param(name = "id", type = "long", desc = "菜单id")
	})
	@Description(desc = "查询菜单接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject jsonObject = new JSONObject();
		MenuVo menuVo = new MenuVo();
		menuVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<MenuVo>(){});
		menuService.saveMenu(menuVo);
		jsonObject.put("id", menuVo.getId());
		return jsonObject;
	}
}
