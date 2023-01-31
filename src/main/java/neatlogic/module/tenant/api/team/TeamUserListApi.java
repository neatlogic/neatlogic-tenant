/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.team;

import java.util.List;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.TeamUserTitle;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.exception.team.TeamNotFoundException;
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class TeamUserListApi extends PrivateApiComponentBase  {

	@Autowired
	private TeamMapper teamMapper;

	@Autowired
	private UserMapper userMapper;
	
	@Override
	public String getToken() {
		return "team/user/list";
	}

	@Override
	public String getName() {
		return "分组用户成员列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
        @Param(name = "teamUuid", type = ApiParamType.STRING, isRequired = true, desc = "分组uuid")
	})
	@Output({
		@Param(name = "teamUserList", explode = UserVo[].class, desc = "分组用户成员列表")
	})
	@Description( desc = "分组用户成员列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String teamUuid = jsonObj.getString("teamUuid");
		if(teamMapper.checkTeamIsExists(teamUuid) == 0) {
			throw new TeamNotFoundException(teamUuid);
		}
		JSONObject resultObj = new JSONObject();
		List<UserVo> teamUserList = userMapper.getUserListByTeamUuid(teamUuid);
		if(CollectionUtils.isNotEmpty(teamUserList)){
			teamUserList.stream().forEach(o -> {
				if(StringUtils.isNotBlank(o.getTitle())){
					o.setTitleText(TeamUserTitle.getText(o.getTitle()));
				}
			});
		}
		resultObj.put("teamUserList", teamUserList);
		return resultObj;
	}

}
