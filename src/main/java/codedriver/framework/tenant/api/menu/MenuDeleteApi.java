package codedriver.framework.tenant.api.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.service.MenuService;

@Service
public class MenuDeleteApi extends ApiComponentBase{

	@Autowired
	private MenuService menuService;
	
	@Override
	public String getToken() {
		return "menuDeleteApi";
	}

	@Override
	public String getName() {
		return "删除租户菜单接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		if(jsonObj==null || !jsonObj.containsKey("menuId")) {
			throw new RuntimeException("不存在参数menuId，请传入正确的menuId");
		}
        Long menuId = jsonObj.getLong("menuId"); 
		JSONObject jsonObject = new JSONObject();
		try {
			int count = this.menuService.checkIsChildern(menuId);
			if (count > 0) {
				jsonObject.put("Status", "ERROR");
				jsonObject.put("Message", "当前菜单含有" + count + "个子菜单，请先移除。");
			} else {
				this.menuService.deleteMenu(menuId);
				jsonObject.put("Status", "OK");
				jsonObject.put("id", menuId);
			}
		} catch (Exception e) {
			jsonObject.put("Status", "OK");
			jsonObject.put("Message", e.getMessage());
		}
		return jsonObject;
	}
}
