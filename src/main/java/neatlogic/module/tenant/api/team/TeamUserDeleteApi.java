package neatlogic.module.tenant.api.team;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.TEAM_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.exception.team.TeamNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = TEAM_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class TeamUserDeleteApi extends PrivateApiComponentBase {

    @Resource
    private TeamMapper teamMapper;

	@Resource
	private UserService userService;

	@Override
	public String getToken() {
		return "team/user/delete";
	}

	@Override
	public String getName() {
		return "分组用户删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
        @Param( name = "teamUuid", isRequired = true, desc = "分组uuid", type = ApiParamType.STRING),
        @Param( name = "userUuidList", isRequired = true, desc = "用户Uuid集合", type = ApiParamType.JSONARRAY)
	})
	@Description( desc = "分组用户删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String teamUuid = jsonObj.getString("teamUuid");
    	if(teamMapper.checkTeamIsExists(teamUuid) == 0) {
			throw new TeamNotFoundException(teamUuid);
		}
    	List<String> userUuidList = jsonObj.getJSONArray("userUuidList").toJavaList(String.class);
    	if (CollectionUtils.isNotEmpty(userUuidList)){
			teamMapper.deleteTeamUserByTeamUuidAndUserUuidList(teamUuid, userUuidList);
    		for (String userUuid: userUuidList){
				userService.updateUserCacheAndSessionByUserUuid(userUuid);
    		}
    	}
		return null;
	}

}
