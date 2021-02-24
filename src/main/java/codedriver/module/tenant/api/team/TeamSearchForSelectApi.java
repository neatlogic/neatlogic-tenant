package codedriver.module.tenant.api.team;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class TeamSearchForSelectApi extends PrivateApiComponentBase {

	@Autowired
	private TeamMapper teamMapper;

	@Override
	public String getToken() {
		return "team/search/forselect";
	}

	@Override
	public String getName() {
		return "查询分组_下拉";
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
			@Param(name = "list",
					type = ApiParamType.STRING,
					explode = ValueTextVo[].class,
					desc = "分组信息"),
			@Param(name = "pageCount",
					type = ApiParamType.INTEGER,
					desc = "总页数"),
			@Param(name = "currentPage",
					type = ApiParamType.INTEGER,
					desc = "当前页数"),
			@Param(name = "pageSize",
					type = ApiParamType.INTEGER,
					desc = "每页展示数量"),
			@Param(name = "rowNum",
					type = ApiParamType.INTEGER,
					desc = "总条目数")
	})
	@Description(desc = "查询分组_下拉")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		
		JSONObject resultObj = new JSONObject();
		TeamVo teamVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<TeamVo>() {});
		if (teamVo.getNeedPage()) {
			int rowNum = teamMapper.searchTeamCount(teamVo);
			resultObj.put("pageSize", teamVo.getPageSize());
			resultObj.put("currentPage", teamVo.getCurrentPage());
			resultObj.put("rowNum", rowNum);
			resultObj.put("pageCount", PageUtil.getPageCount(rowNum, teamVo.getPageSize()));
		}
		List<ValueTextVo> list = new ArrayList<>();
		List<TeamVo> teamList = teamMapper.searchTeam(teamVo);
		if (CollectionUtils.isNotEmpty(teamList)) {
			for (TeamVo team : teamList) {
				ValueTextVo vo = new ValueTextVo();
				vo.setValue(GroupSearch.TEAM.getValuePlugin() + team.getUuid());
				/**
				 * 如果有重名的分组，找出其父分组的名称
				 * 移动端上下滑动时，用户感觉不到分页，所以不能只在当前页寻找重名的分组
				 * 而是应该从全部分组中找当前页是否存在重名的分组
				 **/
				if(teamMapper.checkTeamNameIsRepeated(team) > 0){
					TeamVo parent = teamMapper.getTeamByUuid(team.getParentUuid());
					if(parent != null){
						team.setParentName(parent.getName());
					}
				}
				vo.setText(StringUtils.isNotBlank(team.getParentName())
						? team.getName() + "(" + team.getParentName() + ")" : team.getName());
				list.add(vo);
			}
		}
		resultObj.put("list", list);
		return resultObj;
	}
}
