package codedriver.framework.tenant.api.role;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.AuthAction;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.service.TeamService;

@AuthAction(name="SYSTEM_ROLE_EDIT")
@Service
public class GetRoleTeamListApi extends ApiComponentBase{

	@Autowired
	private TeamService teamService;
	
	@Override
	public String getToken() {
		return "role/getTeamListByRole";
	}

	@Override
	public String getName() {
		return "根据角色获取组织列表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "name", type = "String", desc = "角色名称")})
	@Output({ @Param(name = "id", type = "Long", desc = "菜单ID"),
		@Param(name = "id", type = "Long", desc = "组ID"),
		@Param(name = "pId", type = "Long", desc = "父组ID"),
		@Param(name = "name", type = "String", desc = "组名"),
		@Param(name = "open", type = "boolean", desc = "默认true"),
		@Param(name = "isParent", type = "boolean", desc = "是否父级"),
		@Param(name = "checked", type = "String", desc = "是否被选中"),
		@Param(name = "lft", type = "int", desc = "左编码"),
		@Param(name = "rht", type = "int", desc = "右编码"),
		})
	@Description(desc = "根据角色获取组织列表")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		TeamVo teamVo = new TeamVo();
		if(jsonObj!=null && jsonObj.containsKey("name")) {
			teamVo.setRoleName(jsonObj.getString("name"));
		}else {
			throw new RuntimeException("请传入角色名称name");
		}
		List<TeamVo> teamList = teamService.selectRoleTeamList(teamVo);
		if (teamList.size() == 0) {
			teamVo.setParentId(0l);
			teamList = teamService.selectTeamList(teamVo);
		}
		JSONArray jsonList = new JSONArray();
		for (TeamVo team : teamList) {
			JSONObject json = new JSONObject();
			json.put("id", team.getId());
			json.put("pId", team.getParentId());
			json.put("name", team.getName());
			json.put("open", true);
			json.put("isParent", team.getChildCount() > 0);
			if (team.getRoleName() != null && !team.getRoleName().equals("")) {
				json.put("checked", "true");
			}
			json.put("lft", team.getLft());
			json.put("rht", team.getRht());
			jsonList.add(json);

		}
		return jsonList;
	}
}
