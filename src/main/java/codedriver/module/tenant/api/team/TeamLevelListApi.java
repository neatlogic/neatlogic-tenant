package codedriver.module.tenant.api.team;

import java.util.ArrayList;
import java.util.List;

import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.TeamLevel;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
@Deprecated
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class TeamLevelListApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "team/level/list";
	}

	@Override
	public String getName() {
		return "组织架构等级列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Output({
		@Param(explode = ValueTextVo[].class, desc = "组织架构等级列表")
	})
	@Description(desc = "组织架构等级列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<ValueTextVo> resultList = new ArrayList<>();
		for(TeamLevel level : TeamLevel.values()) {
			resultList.add(new ValueTextVo(level.getValue(), level.getText()));
		}
		return resultList;
	}

}
