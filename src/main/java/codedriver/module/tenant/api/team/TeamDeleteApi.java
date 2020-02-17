package codedriver.module.tenant.api.team;

import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.TeamService;

@AuthAction(name = "SYSTEM_TEAM_EDIT")
@Service
public class TeamDeleteApi extends ApiComponentBase {

	@Autowired
	private TeamService teamService;

	@Override
	public String getToken() {
		return "team/delete";
	}

	@Override
	public String getName() {
		return "删除分组接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "uuidList",
					type = ApiParamType.JSONARRAY,
					desc = "分组uuid集合",
					isRequired = true) })
	@Description(desc = "删除分组接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONArray uuidList = jsonObj.getJSONArray("uuidList");
		for (int i = 0; i < uuidList.size(); i++){
			String teamUuid = uuidList.getString(i);
			teamService.deleteTeam(teamUuid);
		}
		return null;
	}
}
