package codedriver.module.tenant.api.team;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-07 17:39
 **/
@Service
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
        teamMapper.deleteUserTeamByTeamUuid(teamUuid);
        List<String> userUuidList = JSON.parseArray(jsonObj.getString("userUuidList"), String.class);
		if (CollectionUtils.isNotEmpty(userUuidList)){
			for (String userUuid: userUuidList){
				if(userMapper.checkUserIsExists(userUuid) == 0) {
					throw new UserNotFoundException(userUuid);
				}
				teamMapper.insertTeamUser(teamUuid, userUuid);
			}
		}
        return null;
    }
}
