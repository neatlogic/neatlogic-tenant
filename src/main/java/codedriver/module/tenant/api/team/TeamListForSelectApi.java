/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.team;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class TeamListForSelectApi extends PrivateApiComponentBase {

	@Autowired
	private TeamMapper teamMapper;

	@Override
	public String getToken() {
		return "team/list/forselect";
	}

	@Override
	public String getName() {
		return "分组查询接口（下拉框）";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss = true),
			@Param(name = "uuid", type = ApiParamType.STRING, desc = "分组uuid"),
			@Param(name = "parentUuid", type = ApiParamType.STRING, desc = "父分组uuid"),
			@Param(name = "level", type = ApiParamType.STRING, desc = "级别"),
			@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
			@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
			@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
	})
	@Output({
			@Param(name = "teamList", type = ApiParamType.STRING, explode = TeamVo[].class, desc = "分组信息"),
			@Param(explode=BasePageVo.class)
	})
	@Description(desc = "分组查询接口（下拉框）")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		TeamVo teamVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<TeamVo>() {});
		teamVo.setIsDelete(0);
		JSONObject returnObj = new JSONObject();
		if (teamVo.getNeedPage()) {
			int rowNum = teamMapper.searchTeamCount(teamVo);
			returnObj.put("pageSize", teamVo.getPageSize());
			returnObj.put("currentPage", teamVo.getCurrentPage());
			returnObj.put("rowNum", rowNum);
			returnObj.put("pageCount", PageUtil.getPageCount(rowNum, teamVo.getPageSize()));
		}
		List<TeamVo> teamList = teamMapper.searchTeamOrderByNameLengthForSelect(teamVo);
		returnObj.put("tbodyList", teamList);
		return returnObj;
	}

}
