package codedriver.module.tenant.api.user;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.UserService;

@Service
public class UserSearchApi extends ApiComponentBase {

	@Autowired
	private UserService userService;

	@Override
	public String getToken() {
		return "user/search";
	}

	@Override
	public String getName() {
		return "用户查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "keyword",
					type = ApiParamType.STRING,
					desc = "关键字(用户id或名称),模糊查询",
					isRequired = true,
					xss = true),
			@Param(name = "authGroup",
					type = ApiParamType.STRING,
					desc = "权限模块"),
			@Param(name = "auth",
					type = ApiParamType.STRING,
					desc = "权限"),
			@Param(name = "teamUuid",
					type = ApiParamType.STRING,
					desc = "用户组uuid"
			),
			@Param(name = "roleName",
					type = ApiParamType.STRING,
					desc = "角色名称"),
			@Param(name = "currentPage",
					type = ApiParamType.INTEGER,
					desc = "当前页数",
					isRequired = false),
			@Param(name = "pageSize",
					type = ApiParamType.INTEGER,
					desc = "每页展示数量 默认10",
					isRequired = false),
            @Param(name = "needPage",
                    type = ApiParamType.BOOLEAN,
                    desc = "是否分页")})
	@Output({
			@Param(name = "tbodyList",
					type = ApiParamType.JSONARRAY,
					explode = UserVo[].class,
					desc = "table数据列表"),
			@Param(name = "pageCount",
					type = ApiParamType.INTEGER,
					desc = "总页数"),
			@Param(name = "currentPage",
					type = ApiParamType.INTEGER,
					desc = "当前页数"),
			@Param(name = "pageSize",
					type = ApiParamType.INTEGER,
					desc = "每页展示数量")})
	@Description(desc = "查询用户接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		System.out.println(jsonObj.toJSONString());
		JSONObject json = new JSONObject();
		UserVo userVo = new UserVo();
		userVo.setKeyword(jsonObj.getString("keyword"));
		if (jsonObj.containsKey("pageSize")) {
			userVo.setPageSize(jsonObj.getInteger("pageSize"));
		}
		if (jsonObj.containsKey("needPage")){
		    userVo.setNeedPage(jsonObj.getBoolean("needPage"));
        }
        if (jsonObj.containsKey("currentPage")){
			userVo.setCurrentPage(jsonObj.getInteger("currentPage"));
		}
		userVo.setAuth(jsonObj.getString("auth"));
		userVo.setAuthGroup(jsonObj.getString("authGroup"));
		userVo.setTeamUuid(jsonObj.getString("teamUuid"));
		userVo.setRoleName(jsonObj.getString("roleName"));
		List<UserVo> userList = userService.searchUser(userVo);
		json.put("tbodyList", userList);
		if (userVo.getNeedPage()){
            json.put("rowNum", userVo.getRowNum());
            json.put("pageSize", userVo.getPageSize());
            json.put("currentPage", userVo.getCurrentPage());
        }
		return json;
	}
}
