/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.team;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class TeamUserTitleListApi extends PrivateApiComponentBase {
	@Resource
	TeamMapper teamMapper;

	@Override
	public String getToken() {
		return "team/user/title/list";
	}

	@Override
	public String getName() {
		return "组成员头衔列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "teamUuid", type = ApiParamType.STRING, isRequired = true, desc = "组uuid")
	})
	@Output({
		@Param(explode = ValueTextVo[].class, desc = "组成员头衔列表")
	})
	@Description(desc = "组成员头衔列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String teamUuid = jsonObj.getString("teamUuid");
		return teamMapper.getTeamUserTitleListByTeamUuid(teamUuid);
	}

}
