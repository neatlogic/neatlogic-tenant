package codedriver.module.tenant.api.team;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamUserVo;
import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
public class TeamUserDeleteApi extends ApiComponentBase {

    @Autowired
    private TeamMapper teamMapper;

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
        @Param( name = "userUuidList", desc = "用户Uuid集合", type = ApiParamType.JSONARRAY)
	})
	@Description( desc = "分组用户删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String teamUuid = jsonObj.getString("teamUuid");
    	if(teamMapper.checkTeamIsExists(teamUuid) == 0) {
			throw new TeamNotFoundException(teamUuid);
		}
    	List<String> userUuidList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("userUuidList")), String.class);
    	if (CollectionUtils.isNotEmpty(userUuidList)){
    		for (String userUuid: userUuidList){
    			teamMapper.deleteTeamUser(new TeamUserVo(teamUuid, userUuid));
    		}
    	}
		return null;
	}

}