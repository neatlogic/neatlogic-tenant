package codedriver.module.tenant.api.team;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class TeamListApi extends ApiComponentBase {
	@Autowired
	private TeamMapper teamMapper;
	
	@Override
	public String getToken() {
		return "team/list";
	}

	@Override
	public String getName() {
		return "分组列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字模糊查询", isRequired = false, xss = true),
		@Param(name = "valueList", type = ApiParamType.JSONARRAY,  isRequired = false, desc = "用于回显的参数列表"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数", isRequired = false),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页展示数量 默认10", isRequired = false),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页", isRequired = false)
		})
	@Output({
		@Param(name="text", type = ApiParamType.STRING, desc="组名"),
		@Param(name="value", type = ApiParamType.STRING, desc="组uuid")
	})
	@Description(desc = "分组列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<TeamVo> teamList = new ArrayList<TeamVo>();
		if(jsonObj.containsKey("keyword")) {
			TeamVo teamVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<TeamVo>() {});		
			if (teamVo.getNeedPage()) {
				int rowNum = teamMapper.searchTeamCount(teamVo);
				teamVo.setRowNum(rowNum);
				teamVo.setPageCount(PageUtil.getPageCount(rowNum, teamVo.getPageSize()));
			}
			teamList = teamMapper.searchTeam(teamVo);
		}else {//回显
			if(jsonObj.containsKey("valueList") && !jsonObj.getJSONArray("valueList").isEmpty()) {
				List<String> teamUuidList = jsonObj.getJSONArray("valueList").toJavaList(String.class);;
				if(teamUuidList.size()>0) {
					teamList = teamMapper.getTeamByUuidList(teamUuidList);
				}
			}
		}
		return teamList;
	}
}
