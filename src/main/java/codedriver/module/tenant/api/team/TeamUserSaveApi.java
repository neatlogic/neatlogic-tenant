package codedriver.module.tenant.api.team;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.TeamUserVo;
import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-07 17:39
 **/
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class TeamUserSaveApi extends ApiComponentBase {

    @Autowired
    private TeamMapper teamMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "team/user/save";
    }

    @Override
    public String getName() {
        return "分组用户保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param( name = "teamUuid", isRequired = true, desc = "分组uuid", type = ApiParamType.STRING),
            @Param( name = "userUuidList", desc = "用户Uuid集合", type = ApiParamType.JSONARRAY)
    })
    @Description( desc = "分组用户保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	String teamUuid = jsonObj.getString("teamUuid");
    	if(teamMapper.checkTeamIsExists(teamUuid) == 0) {
			throw new TeamNotFoundException(teamUuid);
		}
    	Map<String, String> teamUserTitleMap = new HashMap<>();
		List<TeamUserVo> teamUserList = teamMapper.getTeamUserListByTeamUuid(teamUuid);
		for(TeamUserVo teamUserVo : teamUserList) {
			teamUserTitleMap.put(teamUserVo.getUserUuid(), teamUserVo.getTitle());
		}
        teamMapper.deleteTeamUser(new TeamUserVo(teamUuid));
        List<String> userUuidList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("userUuidList")), String.class);
		if (CollectionUtils.isNotEmpty(userUuidList)){
			List<String> existUserUuidList = userMapper.checkUserUuidListIsExists(userUuidList);
			userUuidList.retainAll(existUserUuidList);
			for (String userUuid: userUuidList){
				String title = teamUserTitleMap.get(userUuid);
				teamMapper.insertTeamUser(new TeamUserVo(teamUuid, userUuid, title));
			}
		}
        return null;
    }
}
