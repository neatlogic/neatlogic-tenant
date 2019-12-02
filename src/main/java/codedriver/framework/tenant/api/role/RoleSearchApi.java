package codedriver.framework.tenant.api.role;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.AuthAction;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.service.RoleService;

@AuthAction(name="SYSTEM_ROLE_EDIT")
@Service
public class RoleSearchApi extends ApiComponentBase{

	@Autowired
	private RoleService roleService;
	
	@Override
	public String getToken() {
		return "role/search";
	}

	@Override
	public String getName() {
		return "查询角色信息接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "keyName", type = ApiParamType.STRING, desc = "关键字,根据关键字查找",isRequired=true),})
	@Output({@Param(name = "name", type = ApiParamType.STRING, desc = "角色名称"),
		@Param(name = "description", type = ApiParamType.STRING, desc = "角色描述"),
		@Param(name = "userCount", type = ApiParamType.INTEGER, desc = "用户数量")
		})
	@Description(desc = "角色查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject json = new JSONObject();
		List<RoleVo> roleList = new ArrayList<>();
		if(jsonObj.containsKey("keyName")) {
			RoleVo roleVo = new RoleVo();//根据关键字查找
			roleVo.setName(jsonObj.getString("keyName"));
			roleList = roleService.getRoleByName(roleVo);
		}else {
			roleList = roleService.selectAllRole();//查找所有
		}		
		json.put("roleList",roleList);
		return json;
	}
}
