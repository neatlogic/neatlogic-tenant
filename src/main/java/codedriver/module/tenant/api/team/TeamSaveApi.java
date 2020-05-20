package codedriver.module.tenant.api.team;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;


@AuthAction(name="SYSTEM_TEAM_EDIT")
@Service
@Transactional
public class TeamSaveApi extends ApiComponentBase{

	@Autowired
	private TeamMapper teamMapper;
	
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
		@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", desc = "组名",isRequired=true, xss=true),
		@Param(name = "parentUuid", type = ApiParamType.STRING, desc = "父级组id"),
		@Param(name = "sort", type = ApiParamType.INTEGER, desc = "排序", isRequired = false),
		@Param(name = "tagIdList", type = ApiParamType.JSONARRAY, desc = "标签ID集合"),
			@Param( name = "userUuidList", type = ApiParamType.JSONARRAY, desc = "用户uuid集合")
		/*@Param(name = "isHandleChildtask", type = ApiParamType.STRING, desc = "是否允许处理下级任务",isRequired=true)*/
	})
	@Output({@Param(name = "uuid", type = ApiParamType.STRING, desc = "保存的组id")})
	@Description(desc = "保存组信息")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject returnObj = new JSONObject();
		String uuid = jsonObj.getString("uuid");
		TeamVo teamVo = new TeamVo();
		teamVo.setName(jsonObj.getString("name"));
		teamVo.setSort(jsonObj.getInteger("sort"));

		if(StringUtils.isNotBlank(uuid)){
			if(teamMapper.checkTeamIsExists(uuid) == 0) {
				throw new TeamNotFoundException(uuid);
			}
			teamVo.setUuid(uuid);
			teamMapper.updateTeamNameByUuid(teamVo);
			teamMapper.deleteTeamTagByUuid(teamVo.getUuid());
		}else {
			String parentUuid = jsonObj.getString("parentUuid");
			if (StringUtils.isNotBlank(parentUuid)){
				if(teamMapper.checkTeamIsExists(parentUuid) == 0) {
					throw new TeamNotFoundException(parentUuid);
				}
			}else {
				parentUuid = TeamVo.DEFAULT_PARENTUUID;
			}
			teamVo.setParentUuid(parentUuid);
			int sort = teamMapper.getMaxTeamSortByParentUuid(parentUuid);
			sort++;
			teamVo.setSort(sort);
			teamMapper.insertTeam(teamVo);
			List<String> userUuidList = JSON.parseArray(jsonObj.getString("userUuidList"), String.class);
			if (CollectionUtils.isNotEmpty(userUuidList)){
				for (String userUuid : userUuidList){
					teamMapper.insertTeamUser(teamVo.getUuid(), userUuid);
				}
			}
		}
		List<Long> tagIdList = JSON.parseArray(jsonObj.getString("tagIdList"), Long.class);
		if(CollectionUtils.isNotEmpty(tagIdList)) {
			for(Long tagId : tagIdList) {
				teamVo.setTagId(tagId);
				teamMapper.insertTeamTag(teamVo);
			}
		}
		
		returnObj.put("uuid", teamVo.getUuid());
		return returnObj;
	}
}
