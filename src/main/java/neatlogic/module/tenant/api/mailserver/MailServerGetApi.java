/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.mailserver;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NOTIFY_CONFIG_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.NotifyConfigMapper;
import neatlogic.framework.dto.MailServerVo;
import neatlogic.framework.dto.NotifyConfigVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
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

	@Input({
			@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "common.id")
	})
	@Output({
		@Param(explode = MailServerVo.class)
	})
	@Description(desc = "nmtam.mailservergetapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long id = jsonObj.getLong("id");
		NotifyConfigVo notifyConfigVo = notifyConfigMapper.getNotifyConfigById(id);
		if (notifyConfigVo != null) {
			String name = notifyConfigVo.getName();
			Integer isActive = notifyConfigVo.getIsActive();
			Integer isDefault = notifyConfigVo.getIsDefault();
			JSONObject config = notifyConfigVo.getConfig();
			String fromAddress = config.getString("fromAddress");
			String homeUrl = config.getString("homeUrl");
			String host = config.getString("host");
			if (StringUtils.isBlank(name)) {
				name = config.getString("name");
			}
			String password = config.getString("password");
			Integer port = config.getInteger("port");
			String sslEnable = config.getString("sslEnable");
			String userName = config.getString("userName");
			MailServerVo mailServerVo = new MailServerVo();
			mailServerVo.setFromAddress(fromAddress);
			mailServerVo.setHomeUrl(homeUrl);
			mailServerVo.setHost(host);
			mailServerVo.setName(name);
			mailServerVo.setPassword(password);
			mailServerVo.setPort(port);
			mailServerVo.setSslEnable(sslEnable);
			mailServerVo.setUserName(userName);
			mailServerVo.setId(id);
			mailServerVo.setIsActive(isActive);
			mailServerVo.setIsDefault(isDefault);
			return mailServerVo;
		}
		return null;
	}

}
