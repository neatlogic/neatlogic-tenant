package codedriver.module.tenant.api.team;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeamTreeApi extends ApiComponentBase {

    @Autowired
    private TeamMapper teamMapper;

    @Override
    public String getToken() {
        return "team/tree";
    }

    @Override
    public String getName() {
        return "用户组织架构树获取接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "uuid", desc = "teamUuid，这里指父级uuid", type = ApiParamType.STRING),
             @Param( name = "currentPage", desc = "当前页", type = ApiParamType.INTEGER),
             @Param( name = "needPage", desc = "是否分页", type = ApiParamType.BOOLEAN),
             @Param( name = "pageSize", desc = "每页最大数", type = ApiParamType.INTEGER)
    })
    @Output({
           @Param( name = "tbodyList", explode = TeamVo[].class, desc = "用户组织架构集合"),
           @Param( explode = BasePageVo.class)
    })
    @Description(desc = "用户组织架构树获取接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
//        TeamVo teamVo = JSON.toJavaObject(jsonObj, TeamVo.class);
        TeamVo teamVo = new TeamVo();
        Boolean needPage = jsonObj.getBoolean("needPage");
        if (needPage != null){
            teamVo.setNeedPage(needPage);
        }
        teamVo.setCurrentPage(jsonObj.getInteger("currentPage"));
        teamVo.setPageSize(jsonObj.getInteger("pageSize"));
        String parentUuid = jsonObj.getString("uuid");
        if (StringUtils.isNotBlank(parentUuid)){
        	if(teamMapper.checkTeamIsExists(parentUuid) == 0) {
        		throw new TeamNotFoundException(parentUuid);
        	}
		}else {
			parentUuid = TeamVo.DEFAULT_PARENTUUID;
		}
    	teamVo.setParentUuid(parentUuid);
		if (teamVo.getNeedPage()){
			int rowNum = teamMapper.searchTeamCount(teamVo);
			returnObj.put("currentPage", teamVo.getCurrentPage());
            returnObj.put("pageCount", PageUtil.getPageCount(rowNum, teamVo.getPageSize()));
            returnObj.put("pageSize", teamVo.getPageSize());
            returnObj.put("rowNum", rowNum);
		}
		List<TeamVo> tbodyList = teamMapper.searchTeam(teamVo);
        returnObj.put("tbodyList", tbodyList);
        return returnObj;
    }
}
