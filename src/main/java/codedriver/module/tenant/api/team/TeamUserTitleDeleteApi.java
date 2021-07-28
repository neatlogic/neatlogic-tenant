/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.team;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserTitleVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.SEARCH)
public class TeamUserTitleDeleteApi extends PrivateApiComponentBase {
	@Resource
	TeamMapper teamMapper;

	@Resource
	UserMapper userMapper;

	@Override
	public String getToken() {
		return "team/user/title/delete";
	}

	@Override
	public String getName() {
		return "组成员头衔删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "teamUuid", type = ApiParamType.STRING, isRequired = true, desc = "组uuid"),
			@Param(name = "title", type = ApiParamType.STRING, isRequired = true, desc = "头衔")
	})
	@Output({
		@Param(explode = ValueTextVo[].class, desc = "组成员头衔删除接口")
	})
	@Description(desc = "组成员头衔删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String teamUuid = jsonObj.getString("teamUuid");
		String title = jsonObj.getString("title");
		UserTitleVo userTitleVo = userMapper.getUserTitleLockByName(title);
		if(userTitleVo != null) {
			teamMapper.deleteTeamUserTitleByTeamUuidAndTitle(teamUuid, userTitleVo.getId());
			if (teamMapper.checkTitleIsReferenceByTitleId(userTitleVo.getId()) == 0) {
				userMapper.deleteUserTitleByName(userTitleVo.getName());
			}
		}
		return null;
	}

}
