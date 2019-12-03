package codedriver.framework.tenant.api.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.AuthAction;
import codedriver.framework.exception.MenuExceptionMessage;
import codedriver.framework.exception.core.ApiRuntimeException;
import codedriver.framework.exception.core.FrameworkExceptionMessageBase;
import codedriver.framework.exception.type.CustomExceptionMessage;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.dto.MenuVo;
import codedriver.framework.tenant.service.MenuService;

@Service
@AuthAction(name="SYSTEM_MENU_EDIT")
public class MenuSaveApi extends ApiComponentBase{

	@Autowired
	private MenuService menuService;
	
	@Override
	public String getToken() {
		return "menu/save";
	}

	@Override
	public String getName() {
		return "保存菜单接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ 
		@Param(name = "id", type = ApiParamType.LONG, desc = "菜单id",isRequired = false),
		@Param(name = "parentId", type = ApiParamType.LONG, desc = "父节点id",isRequired = true) ,
		@Param(name = "name", type = ApiParamType.STRING, desc = "菜单名称",isRequired = true),
		@Param(name = "url", type = ApiParamType.STRING, desc = "菜单url",isRequired = true),
		@Param(name = "description", type = ApiParamType.STRING, desc = "菜单描述",isRequired = true),
		@Param(name = "module", type = ApiParamType.STRING, desc = "模块名",isRequired = true),
		@Param(name = "isActive", type = ApiParamType.LONG, desc = "是否启用，0:正常，1:禁用",isRequired = true),
		@Param(name = "isAuto", type = ApiParamType.LONG, desc = "是否自动打开，0:否，1:是",isRequired = true),
		@Param(name = "openMode", type = ApiParamType.STRING, desc = "打开页面方式，tab:打开新tab页面   blank:打开新标签页",isRequired = true),
		@Param(name = "icon", type = ApiParamType.STRING, desc = "目录对应的图标class",isRequired = true), 
		@Param(name = "roleNameList", type = ApiParamType.JSONARRAY, desc = "角色",isRequired = false) 
		})
	@Output({
		@Param(name = "id", type = ApiParamType.LONG, desc = "菜单id")
	})
	@Description(desc = "保存菜单接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject jsonObject = new JSONObject();
		MenuVo menuVo = new MenuVo();
		menuVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<MenuVo>(){});
		if(menuVo.getId() == menuVo.getParentId()) {
			throw new ApiRuntimeException(new FrameworkExceptionMessageBase(new MenuExceptionMessage(new CustomExceptionMessage(" 菜单id不合法，不能与父菜单id相同"))));
		}
		menuService.saveMenu(menuVo);
		jsonObject.put("id", menuVo.getId());
		return jsonObject;
	}
}
