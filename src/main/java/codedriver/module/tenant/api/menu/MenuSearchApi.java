package codedriver.module.tenant.api.menu;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.auth.label.MENU_MODIFY;
import codedriver.module.tenant.dao.mapper.MenuMapper;
import codedriver.module.tenant.dto.MenuVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = MENU_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class MenuSearchApi extends PrivateApiComponentBase {
	
	@Autowired
	private MenuMapper menuMapper;

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
		@Param(name = "id", type = ApiParamType.LONG, desc = "菜单id", isRequired = false), 
		@Param(name = "parentId", type = ApiParamType.LONG, desc = "菜单父节点id", isRequired = false), 
		@Param(name = "type", type = ApiParamType.INTEGER, desc = "默认1，1:返回全部菜单，0:根据角色返回", isRequired = false),
		@Param(name = "isAll", type = ApiParamType.INTEGER, desc = "是否返回所有菜单，默认1，0不启用的菜单不返回，如果父节点不激活，则它所有子节点都不反回；1返回所有菜单。", isRequired = false)
		})
	@Output({ 
		@Param(name = "menuList", explode = MenuVo[].class, desc = "菜单列表")
		})
	@Description(desc = "查询菜单接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject json = new JSONObject();
		List<String> roleUuidList = new ArrayList<String>();
		Integer isActive = null;
		// 如果是根据角色返回对应菜单
		if (jsonObj.containsKey("type") && jsonObj.getIntValue("type") == 0) {
			UserContext userContext = UserContext.get();
			roleUuidList = userContext.getRoleUuidList();
		}
		if (jsonObj.containsKey("isAll") &&jsonObj.getIntValue("isAll") == 0) {
			isActive = 1;
		}
		List<MenuVo> menuList = menuMapper.getMenuList(new MenuVo(jsonObj.getLong("id"), jsonObj.getLong("parentId"), isActive, roleUuidList));
		json.put("menuList", menuList);
		return json;
	}
}
