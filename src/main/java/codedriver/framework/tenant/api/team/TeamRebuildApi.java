package codedriver.framework.tenant.api.team;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.service.TeamService;

@AuthAction(name="SYSTEM_TEAM_EDIT")
@Service
public class TeamRebuildApi extends ApiComponentBase{

	@Autowired
	private TeamService teamService;
	
	@Override
	public String getToken() {
		return "team/rebuild";
	}

	@Override
	public String getName() {
		return "重建前序排列接口";
	}
	
	@Override
	public String getConfig() {
		return null;
	}


	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "组id",isRequired=true)})
	@Output({@Param(name = "id", type = ApiParamType.STRING, desc = "组id"),
		@Param(name = "name", type = ApiParamType.STRING, desc = "组名"),
		@Param(name = "description", type = ApiParamType.STRING, desc = "组描述"),
		@Param(name = "isHandletask", type = ApiParamType.STRING, desc = "是否允许处理下级任务"),
		@Param(name = "moduleList", type = ApiParamType.STRING, desc = "关联模块")})
	@Description(desc = "获取组信息")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject json = new JSONObject();
		teamService.rebuildLeftRightCode(null, 0);
		return null;
	}
}

