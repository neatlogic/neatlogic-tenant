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

package neatlogic.module.tenant.api.integration;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.integration.dto.IntegrationVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Deprecated
@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationListApi extends PrivateApiComponentBase {

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "integration/list";
	}

	@Override
	public String getName() {
		return "集成设置数据列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "handler", type = ApiParamType.STRING, desc = "组件")
	})
	@Output({ @Param(explode = IntegrationVo[].class, desc = "集成设置列表") })
	@Description(desc = "集成设置数据列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		IntegrationVo integrationVo = new IntegrationVo();
		integrationVo.setNeedPage(false);
		integrationVo.setIsActive(null);
		integrationVo.setHandler(jsonObj.getString("handler"));
		return integrationMapper.searchIntegration(integrationVo);
	}

}
