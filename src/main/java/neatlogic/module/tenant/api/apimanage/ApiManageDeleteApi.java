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

package neatlogic.module.tenant.api.apimanage;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.auth.label.INTERFACE_MODIFY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.ApiNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.dao.mapper.ApiMapper;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.module.tenant.exception.api.ApiNotAllowedToDeleteException;

@Service
@Transactional
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class ApiManageDeleteApi extends PrivateApiComponentBase {
	
	@Autowired
	private ApiMapper ApiMapper;
	
	@Override
	public String getToken() {
		return "apimanage/delete";
	}

	@Override
	public String getName() {
		return "接口删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "token", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "接口token")
	})
	@Description(desc = "接口删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String token = jsonObj.getString("token");
		ApiVo apiVo = PrivateApiComponentFactory.getApiByToken(token);
		//内存中的接口不允许删除
		if(apiVo != null) {
			throw new ApiNotAllowedToDeleteException(token);
		}
		apiVo = ApiMapper.getApiByToken(token);
		if(apiVo == null) {
			throw new ApiNotFoundException(token);
		}
		ApiMapper.deleteApiByToken(token);
		return null;
	}

}
