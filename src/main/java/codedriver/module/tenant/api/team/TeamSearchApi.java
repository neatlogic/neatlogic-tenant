package codedriver.module.tenant.api.team;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.TeamService;

@Service
public class TeamSearchApi extends ApiComponentBase {

	@Autowired
	private TeamService teamService;

	@Override
	public String getToken() {
		return "team/search";
	}

	@Override
	public String getName() {
		return "分组查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "keyword",
					type = ApiParamType.STRING,
					desc = "关键字",
					isRequired = false,
					xss = true),
			@Param(name = "uuid",
					type = ApiParamType.STRING,
					desc = "分组uuid",
					isRequired = false),
			@Param(name = "parentUuid",
					type = ApiParamType.STRING,
					desc = "父分组uuid",
					isRequired = false),
			@Param(name = "currentPage",
					type = ApiParamType.INTEGER,
					desc = "当前页",
					isRequired = false),
			@Param(name = "pageSize",
					type = ApiParamType.INTEGER,
					desc = "每页数据条目",
					isRequired = false),
			@Param(name = "needPage",
				type = ApiParamType.BOOLEAN,
				desc = "是否需要分页，默认true",
				isRequired = false) 
})
	@Output({
			@Param(name = "teamList",
					type = ApiParamType.STRING,
					explode = TeamVo[].class,
					desc = "分组信息"),
			@Param(name = "pageSize",
					type = ApiParamType.INTEGER,
					desc = "每页数据条目"),
			@Param(name = "currentPage",
					type = ApiParamType.INTEGER,
					desc = "当前页"),
			@Param(name = "rowNum",
					type = ApiParamType.INTEGER,
					desc = "返回条目总数"),
			@Param(name = "pageCount",
					type = ApiParamType.INTEGER,
					desc = "页数") })
	@Description(desc = "分组查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		TeamVo teamVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<TeamVo>() {});
		List<TeamVo> teamList = teamService.searchTeam(teamVo);
		JSONObject returnObj = new JSONObject();
		returnObj.put("teamList", teamList);
		if (teamVo.getNeedPage()) {
			returnObj.put("pageSize", teamVo.getPageSize());
			returnObj.put("currentPage", teamVo.getCurrentPage());
			returnObj.put("rowNum", teamVo.getRowNum());
			returnObj.put("pageCount", teamVo.getPageCount());
		}
		return returnObj;
	}

}
