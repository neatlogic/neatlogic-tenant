package codedriver.framework.tenant.api.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.dto.MenuVo;
import codedriver.framework.tenant.service.MenuService;

@Service
public class MenuSaveApi extends ApiComponentBase{

	@Autowired
	private MenuService menuService;
	
	@Override
	public String getToken() {
		return "menuSaveApi";
	}

	@Override
	public String getName() {
		return "保存租户菜单接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String[] params = {"parentId","name","url","description","type","isActive","closable","defaultOpen","newOpen","classPath" };
		//CommonUtil.verificationParams(params, jsonObj);
		JSONObject jsonObject = new JSONObject();
		MenuVo menuVo = new MenuVo();
		if(jsonObj!=null) {
			menuVo.setId(jsonObj.getLong("id"));
			menuVo.setParentId(jsonObj.getLong("parentId"));
			menuVo.setName(jsonObj.getString("name"));
			menuVo.setUrl(jsonObj.getString("url"));
			menuVo.setDescription(jsonObj.getString("description"));
			menuVo.setModule(jsonObj.getString("module"));
			menuVo.setIsActive(jsonObj.getInteger("isActive"));
			menuVo.setClosable(jsonObj.getInteger("closable"));
			menuVo.setDefaultOpen(jsonObj.getInteger("defaultOpen"));
			menuVo.setNewOpen(jsonObj.getInteger("newOpen"));
			menuVo.setClassPath(jsonObj.getString("classPath"));
		}

		try {
			menuService.saveMenu(menuVo);
			jsonObject.put("id", menuVo.getId());
			jsonObject.put("Status", "OK");
		} catch (Exception e) {
			jsonObject.put("Status", "ERROR");
			jsonObject.put("Message", e.getMessage());
		}
		return jsonObject;
	}
}
