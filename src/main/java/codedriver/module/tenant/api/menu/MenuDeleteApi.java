package codedriver.module.tenant.api.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.exception.core.ApiRuntimeException;
import codedriver.framework.exception.core.FrameworkExceptionMessageBase;
import codedriver.framework.exception.type.CustomExceptionMessage;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.exception.MenuExceptionMessage;
import codedriver.module.tenant.service.MenuService;

@Service
@AuthAction(name="SYSTEM_MENU_EDIT")
public class MenuDeleteApi extends ApiComponentBase{

	@Autowired
	private MenuService menuService;
	
	@Override
	public String getToken() {
		return "menu/delete";
	}

	@Override
	public String getName() {
		return "删除菜单接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "id", type = ApiParamType.LONG, desc = "菜单id",isRequired = true) })
	@Output({})
	@Description(desc = "删除菜单接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
        Long menuId = jsonObj.getLong("id"); 
		JSONObject jsonObject = new JSONObject();
		int count = this.menuService.checkIsChildern(menuId);
		if (count > 0) {
			throw new ApiRuntimeException(new FrameworkExceptionMessageBase(new MenuExceptionMessage(new CustomExceptionMessage("当前菜单含有" + count + "个子菜单，请先移除。"))));
		} else {
			this.menuService.deleteMenu(menuId);
		}
		return jsonObject;
	}
}
