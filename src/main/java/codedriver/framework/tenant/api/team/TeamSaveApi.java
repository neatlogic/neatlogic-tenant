package codedriver.framework.tenant.api.team;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
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
public class TeamSaveApi extends ApiComponentBase{

	@Autowired
	private TeamService teamService;
	
	@Override
	public String getToken() {
		return "team/save";
	}

	@Override
	public String getName() {
		return "保存组信息";
	}
	
	@Override
	public String getConfig() {
		return null;
	}


	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "组id",isRequired=false),
		@Param(name = "name", type = ApiParamType.STRING, desc = "组名",isRequired=true),
		@Param(name = "parentId", type = ApiParamType.STRING, desc = "父级组id",isRequired=true),
		@Param(name = "description", type = ApiParamType.STRING, desc = "组描述",isRequired=true),
		@Param(name = "isHandleChildtask", type = ApiParamType.STRING, desc = "是否允许处理下级任务",isRequired=true),
		@Param(name = "moduleList", type = ApiParamType.JSONARRAY, desc = "关联模块，多个逗号隔开",isRequired=false)
	})
	@Output({})
	@Description(desc = "保存组信息")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		TeamVo teamVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<TeamVo>() {
		});
		// 保存组模块
		List<String> list = JSONObject.parseArray(jsonObj.getString("moduleList"), String.class);
		teamVo.setModuleList(list);
		if (jsonObj.getString("uuid") == null) {
			teamService.insertTeam(teamVo);
		} else {
			teamService.updateTeam(teamVo);
		}
		return null;
	}
}
