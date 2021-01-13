package codedriver.module.tenant.api.team;

import java.util.List;

import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class TeamSearchApi extends PrivateApiComponentBase {

	@Autowired
	private TeamMapper teamMapper;

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
					xss = true),
			@Param(name = "uuid",
					type = ApiParamType.STRING,
					desc = "分组uuid",
					isRequired = false),
			@Param(name = "parentUuid",
					type = ApiParamType.STRING,
					desc = "父分组uuid",
					isRequired = false),
			@Param(name = "level",
					type = ApiParamType.STRING,
					desc = "级别",
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
			@Param(explode=BasePageVo.class)
	})
	@Description(desc = "分组查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		TeamVo teamVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<TeamVo>() {});
		JSONObject returnObj = new JSONObject();
		if (teamVo.getNeedPage()) {
			int rowNum = teamMapper.searchTeamCount(teamVo);
			returnObj.put("pageSize", teamVo.getPageSize());
			returnObj.put("currentPage", teamVo.getCurrentPage());
			returnObj.put("rowNum", rowNum);
			returnObj.put("pageCount", PageUtil.getPageCount(rowNum, teamVo.getPageSize()));
		}
		List<TeamVo> teamList = teamMapper.searchTeam(teamVo);
		returnObj.put("teamList", teamList);
		return returnObj;
	}

}
