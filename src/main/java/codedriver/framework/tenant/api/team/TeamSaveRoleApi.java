package codedriver.framework.tenant.api.team;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.AuthAction;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Example;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.service.TeamService;

@AuthAction(name="SYSTEM_TEAM_EDIT")
@Service
public class TeamSaveRoleApi extends ApiComponentBase{

	@Autowired
	private TeamService teamService;
	
	@Override
	public String getToken() {
		return "team/save/role";
	}

	@Override
	public String getName() {
		return "保存组角色";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "roleName", type = ApiParamType.STRING, desc = "角色名称",isRequired=true),
			@Param(name = "nodeJson", type = ApiParamType.STRING, desc = "角色和组信息",isRequired=true),})
	@Output({})
	@Example(example="{\r\n" + 
			"	\"nodeJson\":\"[{'uuid':130,'roleName':'R_TEST'},{'uuid':140,'roleName':'R_TEST'},{'uuid':150,'roleName':'R_TEST'}]\",\r\n" + 
			"	\"roleName\":\"R_TEST\"\r\n" + 
			"}")
	@Description(desc = "保存组角色")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject json = new JSONObject();
		String nodeJson = jsonObj.getString("nodeJson");
		String roleName = jsonObj.getString("roleName");
		try {
			JSONArray nodeArray = JSONArray.parseArray(nodeJson);
			List<TeamVo> list = JSONObject.parseArray(nodeArray.toJSONString(), TeamVo.class);
			this.teamService.updateTeamRole(list, roleName);
			json.put("Status", "OK");
		} catch (Exception e) {
			json.put("Status", "Error");
			throw new RuntimeException(e);
		}
		return json;
	}
}


