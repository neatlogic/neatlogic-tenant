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

package neatlogic.module.tenant.api.mailserver;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NOTIFY_CONFIG_MODIFY;
import neatlogic.framework.dao.mapper.NotifyConfigMapper;
import neatlogic.framework.notify.core.NotifyHandlerType;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.dto.MailServerVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = NOTIFY_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class MailServerGetApi extends PrivateApiComponentBase {

	@Resource
	private NotifyConfigMapper notifyConfigMapper;

	@Override
	public String getToken() {
		return "mailserver/get";
	}

	@Override
	public String getName() {
		return "nmtam.mailservergetapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({})
	@Output({
		@Param(explode = MailServerVo.class)
	})
	@Description(desc = "nmtam.mailservergetapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String config = notifyConfigMapper.getConfigByType(NotifyHandlerType.EMAIL.getValue());
		if (StringUtils.isBlank(config)) {
			return null;
		}
		MailServerVo mailServerVo = JSONObject.parseObject(config, MailServerVo.class);
		return mailServerVo;
	}

}
