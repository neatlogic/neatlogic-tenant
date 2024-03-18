/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.team;

import java.util.ArrayList;
import java.util.List;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.TeamLevel;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
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
