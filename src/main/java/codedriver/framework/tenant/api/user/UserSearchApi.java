package codedriver.framework.tenant.api.user;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.AuthAction;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.service.UserAccountService;

@AuthAction(name="SYSTEM_USER_EDIT")
@Service
public class UserSearchApi extends ApiComponentBase{
	
	@Autowired
	private UserAccountService userService;
	
	@Override
	public String getToken() {
		return "user/search";
	}

	@Override
	public String getName() {
		return "查询用户接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	
	@Input({ @Param(name = "userName", type = ApiParamType.STRING, desc = "关键字(用户id或名称),模糊查询",isRequired=false),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数",isRequired=false),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页展示数量 默认10",isRequired=true)})
	@Output({ @Param(name = "userList", type = ApiParamType.JSONARRAY, desc = "用户信息list"),
		@Param(name = "pageCount", type = ApiParamType.INTEGER, desc = "总页数"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页展示数量"),
		@Param(name = "userId", type = ApiParamType.STRING, desc = "用户Id"),
		@Param(name = "userName", type = ApiParamType.STRING, desc = "用户名"),
		@Param(name = "email", type = ApiParamType.STRING, desc = "邮箱"),
		@Param(name = "phone", type = ApiParamType.STRING, desc = "电话")
		})
	@Description(desc = "查询用户接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject json = new JSONObject();
		UserVo userVo = new UserVo();
		userVo.setUserName(jsonObj.getString("userName"));
		userVo.setPageSize(jsonObj.getInteger("pageSize"));
		userVo.setCurrentPage(jsonObj.getInteger("currentPage"));
		Map<String, Object> resultMap = userService.getUserList(userVo);
		json.put("userList", resultMap.get("resultList"));
		json.put("pageCount", resultMap.get("pageCount"));
		json.put("pageSize", userVo.getPageSize());
		json.put("currentPage", userVo.getCurrentPage());
		return json;
	}
}
