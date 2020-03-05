package codedriver.module.tenant.api.team;

import codedriver.framework.dto.TagVo;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.TeamService;

import java.util.ArrayList;
import java.util.List;


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
		@Param(name = "name", type = ApiParamType.STRING, desc = "组名",isRequired=true, xss=true),
		@Param(name = "parentUuId", type = ApiParamType.STRING, desc = "父级组id"),
		@Param(name = "sort", type = ApiParamType.INTEGER, desc = "排序", isRequired = true),
		@Param(name = "tagIdList", type = ApiParamType.JSONARRAY, desc = "标签ID集合")
		/*@Param(name = "isHandleChildtask", type = ApiParamType.STRING, desc = "是否允许处理下级任务",isRequired=true)*/
	})
	@Output({@Param(name = "uuid", type = ApiParamType.STRING, desc = "保存的组id")})
	@Description(desc = "保存组信息")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject json = new JSONObject();
		TeamVo teamVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<TeamVo>() {
		});
		if (jsonObj.containsKey("tagIdList")){
			List<TagVo> tagList = new ArrayList<>();
			JSONArray tagIdList = jsonObj.getJSONArray("tagIdList");
			for (int i = 0; i < tagIdList.size(); i++){
				Long tagId = tagIdList.getLong(i);
				TagVo tagVo = new TagVo();
				tagVo.setId(tagId);
				tagList.add(tagVo);
			}
			teamVo.setTagList(tagList);
		}
		teamService.saveTeam(teamVo);

		json.put("uuid", teamVo.getUuid());
		return json;
	}
}
