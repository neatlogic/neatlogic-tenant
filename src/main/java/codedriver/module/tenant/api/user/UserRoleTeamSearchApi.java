package codedriver.module.tenant.api.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.RoleService;
import codedriver.module.tenant.service.TeamService;
import codedriver.module.tenant.service.UserService;
@Service
public class UserRoleTeamSearchApi extends ApiComponentBase {

	@Autowired
	private UserService userService;
	@Autowired
	private RoleService roleService;
	@Autowired
	private TeamService teamService;
	
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private RoleMapper roleMapper;
	@Autowired
	private TeamMapper teamMapper;
	
	@Override
	public String getToken() {
		return "user/role/team/search";
	}

	@Override
	public String getName() {
		return "用户角色及组织架构查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字(用户id或名称),模糊查询", isRequired = false, xss = true),
		@Param(name = "userIdList", type = ApiParamType.JSONARRAY,  isRequired = false, desc = "用户id列表"),
		@Param(name = "roleNameList", type = ApiParamType.JSONARRAY,  isRequired = false, desc = "角色名称列表"),
		@Param(name = "teamUuidList", type = ApiParamType.JSONARRAY,  isRequired = false, desc = "组uuid列表"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数", isRequired = false),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页展示数量 默认10", isRequired = false),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页", isRequired = false)
		})
	@Output({
		@Param(name="userList", explode=UserVo[].class, desc="用户列表"),
		@Param(name="roleList", explode=RoleVo[].class, desc="角色列表"),
		@Param(name="teamList", explode=TeamVo[].class, desc="组织架构列表"),
	})
	@Description(desc = "用户角色及组织架构查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<UserVo> userList = null;
		List<RoleVo> roleList = null;
		List<TeamVo> teamList = null;
		if(jsonObj.containsKey("keyword")) {
			UserVo userVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<UserVo>() {});
			userList = userService.searchUser(userVo);
			
			RoleVo roleVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<RoleVo>() {});
			roleList = roleService.searchRole(roleVo);
			
			TeamVo teamVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<TeamVo>() {});		
			teamList = teamService.searchTeam(teamVo);
		}else {
			if(jsonObj.containsKey("userIdList") && !jsonObj.getJSONArray("userIdList").isEmpty()) {
				List<String> userIdList = JSON.parseArray(jsonObj.getJSONArray("userIdList").toJSONString(), String.class);
				userList = userMapper.getUserByUserIdList(userIdList);
			}
			if(jsonObj.containsKey("roleNameList") && !jsonObj.getJSONArray("roleNameList").isEmpty()) {
				List<String> roleNameList = JSON.parseArray(jsonObj.getJSONArray("roleNameList").toJSONString(), String.class);
				roleList = roleMapper.getRoleByRoleNameList(roleNameList);
			}
			if(jsonObj.containsKey("teamUuidList") && !jsonObj.getJSONArray("teamUuidList").isEmpty()) {
				List<String> teamUuidList = JSON.parseArray(jsonObj.getJSONArray("teamUuidList").toJSONString(), String.class);
				teamList = teamMapper.getTeamByUuidList(teamUuidList);
			}
		}
		
		
		JSONObject resultObj = new JSONObject();
		resultObj.put("userList", userList);
		resultObj.put("roleList", roleList);
		resultObj.put("teamList", teamList);
		return resultObj;
	}

}
