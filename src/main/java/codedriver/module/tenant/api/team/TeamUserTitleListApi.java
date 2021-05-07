/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.team;

import java.util.ArrayList;
import java.util.List;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.TeamUserTitle;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Deprecated
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class TeamUserTitleListApi extends PrivateApiComponentBase {

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
	@Output({
		@Param(explode = ValueTextVo[].class, desc = "组成员头衔列表")
	})
	@Description(desc = "组成员头衔列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<ValueTextVo> resultList = new ArrayList<>();
		for(TeamUserTitle title : TeamUserTitle.values()) {
			resultList.add(new ValueTextVo(title.getValue(), title.getText()));
		}
		return resultList;
	}

}
