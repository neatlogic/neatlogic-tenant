package codedriver.module.tenant.api.menu;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.auth.label.MENU_MODIFY;
import codedriver.module.tenant.exception.menu.MenuDeleteException;
import codedriver.module.tenant.service.MenuService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = MENU_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class MenuDeleteApi extends PrivateApiComponentBase{

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
			throw new MenuDeleteException("当前菜单含有" + count + "个子菜单，请先移除。");
		} else {
			this.menuService.deleteMenu(menuId);
		}
		return jsonObject;
	}
}
