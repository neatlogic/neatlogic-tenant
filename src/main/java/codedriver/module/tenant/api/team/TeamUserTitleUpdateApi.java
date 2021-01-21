package codedriver.module.tenant.api.team;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import codedriver.framework.auth.label.TEAM_MODIFY;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.TeamUserTitle;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.TeamUserVo;
import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.exception.team.TeamUserTitleNotFoundException;
import codedriver.framework.exception.user.UserNotFoundException;
@Service
@Transactional
@AuthAction(action = TEAM_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class TeamUserTitleUpdateApi extends PrivateApiComponentBase {

	@Autowired
	private TeamMapper teamMapper;
	
	@Autowired
	private UserMapper userMapper;
	
	@Override
	public String getToken() {
		return "team/user/title/update";
	}

	@Override
	public String getName() {
		return "组用户头衔更新接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "teamUuid", type = ApiParamType.STRING, isRequired = true, desc = "组uuid"),
		@Param(name = "userUuid", type = ApiParamType.STRING, isRequired = true, desc = "用户uuid"),
		@Param(name = "title", type = ApiParamType.STRING, desc = "头衔")
	})
	@Output({
		@Param(name="Return", explode = TeamUserVo.class, desc = "组用户头衔信息")
	})
	@Description(desc = "组用户头衔更新接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		TeamUserVo teamUserVo = JSON.toJavaObject(jsonObj, TeamUserVo.class);
		if(teamMapper.checkTeamIsExists(teamUserVo.getTeamUuid()) == 0) {
			throw new TeamNotFoundException(teamUserVo.getTeamUuid());
		}
		if(userMapper.checkUserIsExists(teamUserVo.getUserUuid()) == 0) {
			throw new UserNotFoundException(teamUserVo.getUserUuid());
		}
		if(StringUtils.isNotBlank(teamUserVo.getTitle()) && TeamUserTitle.getValue(teamUserVo.getTitle()) == null) {
			throw new TeamUserTitleNotFoundException(teamUserVo.getTitle());
		}
		teamMapper.updateTeamUserTitle(teamUserVo);
		return teamUserVo;
	}

}
