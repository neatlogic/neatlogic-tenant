package codedriver.framework.tenant.api.team;
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

@AuthAction(name="SYSTEM_TEAM_EDIT")
@Service
public class TeamSearchForTreeApi extends ApiComponentBase{

	@Autowired
	private TeamService teamService;
	
	@Override
	public String getToken() {
		return "team/tree/search";
	}

	@Override
	public String getName() {
		return "查询组接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "roleName", type = "String", desc = "角色名称",isRequired="false"),
			@Param(name = "parentId", type = "String", desc = "父组Id",isRequired="false")})
	@Output({@Param(name = "id", type = "String", desc = "组uuid"),
		@Param(name = "pId", type = "String", desc = "父组Id"),
		@Param(name = "name", type = "String", desc = "组名"),
		@Param(name = "open", type = "boolean", desc = "是否打开"),
		@Param(name = "isParent", type = "boolean", desc = "是否是父级"),
		@Param(name = "lft", type = "int", desc = "左编码"),
		@Param(name = "rht", type = "int", desc = "右编码")})
	@Description(desc = "查询组接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		TeamVo teamVo = new TeamVo();
		teamVo.setRoleName(jsonObj.getString("roleName"));
		teamVo.setParentId(jsonObj.getString("parentId"));
		List<TeamVo> teamList = teamService.selectTeamList(teamVo);
		JSONArray jsonList = new JSONArray();
		for (TeamVo team : teamList) {
			JSONObject json = new JSONObject();
			json.put("id", team.getUuid());
			json.put("pId", team.getParentId());
			json.put("name", team.getName());
			json.put("open", true);
			json.put("isParent", team.getChildCount() > 0);
			json.put("lft", team.getLft());
			json.put("rht", team.getRht());
			jsonList.add(json);
		}
		return jsonList;
	}
}

