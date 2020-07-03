package codedriver.module.tenant.api.team;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.TeamLevel;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.TeamUserVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.exception.team.TeamLevelNotFoundException;
import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.TeamService;


@AuthAction(name="SYSTEM_TEAM_EDIT")
@Service
@Transactional
public class TeamSaveApi extends ApiComponentBase{

	@Autowired
	private TeamMapper teamMapper;
    
    @Autowired
    private UserMapper userMapper;
    
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


	@Input({ 
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "组id",isRequired=false),
		@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", desc = "组名",isRequired=true, xss=true),
		@Param(name = "parentUuid", type = ApiParamType.STRING, desc = "父级组id"),
		@Param(name = "level", type = ApiParamType.STRING, isRequired = true, desc = "层级"),
		@Param(name = "tagIdList", type = ApiParamType.JSONARRAY, desc = "标签ID集合"),
		@Param( name = "userUuidList", type = ApiParamType.JSONARRAY, desc = "用户uuid集合")
	})
	@Output({@Param(name = "uuid", type = ApiParamType.STRING, desc = "保存的组id")})
	@Description(desc = "保存组信息")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject returnObj = new JSONObject();
		String level = jsonObj.getString("level");
		if(TeamLevel.getValue(level) == null) {
			throw new TeamLevelNotFoundException(level);
		}
		String uuid = jsonObj.getString("uuid");
		TeamVo teamVo = new TeamVo();
		teamVo.setName(jsonObj.getString("name"));
		teamVo.setLevel(level);
		if(StringUtils.isNotBlank(uuid)){
			if(teamMapper.checkTeamIsExists(uuid) == 0) {
				throw new TeamNotFoundException(uuid);
			}
			teamVo.setUuid(uuid);
			teamMapper.updateTeamNameByUuid(teamVo);
			teamMapper.deleteTeamTagByUuid(teamVo.getUuid());
		}else {
//			teamMapper.getTeamLockByUuid(TeamVo.ROOT_UUID);
			if(!teamService.checkLeftRightCodeIsExists()) {
				teamService.rebuildLeftRightCode(TeamVo.ROOT_UUID, 0);
			}
			String parentUuid = jsonObj.getString("parentUuid");
			if (StringUtils.isBlank(parentUuid)){
				parentUuid = TeamVo.ROOT_UUID;
			}
			TeamVo parentTeam;
			if("0".equals(parentUuid)){
				parentTeam = teamService.buildRootTeam();
			}else{
				parentTeam = teamMapper.getTeamByUuid(parentUuid);
			}

			if(parentTeam == null) {
				throw new TeamNotFoundException(parentUuid);
			}
			teamVo.setParentUuid(parentUuid);
			teamVo.setLft(parentTeam.getRht());
			teamVo.setRht(teamVo.getLft() + 1);
			//更新插入位置右边的左右编码值
			teamMapper.batchUpdateTeamLeftCode(teamVo.getLft(), 2);
			teamMapper.batchUpdateTeamRightCode(teamVo.getLft(), 2);

			teamMapper.insertTeam(teamVo);
			List<String> userUuidList = JSON.parseArray(jsonObj.getString("userUuidList"), String.class);
			if (CollectionUtils.isNotEmpty(userUuidList)){
				for (String userUuid : userUuidList){
					if(userMapper.checkUserIsExists(userUuid) == 0) {
						throw new UserNotFoundException(userUuid);
					}
					teamMapper.insertTeamUser(new TeamUserVo(teamVo.getUuid(), userUuid));
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
