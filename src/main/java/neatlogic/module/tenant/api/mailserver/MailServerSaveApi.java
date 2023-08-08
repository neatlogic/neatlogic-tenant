/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.mailserver;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NOTIFY_CONFIG_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.NotifyConfigMapper;
import neatlogic.framework.dto.MailServerVo;
import neatlogic.framework.notify.core.NotifyHandlerType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@AuthAction(action = NOTIFY_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class MailServerSaveApi extends PrivateApiComponentBase {

	@Resource
	private NotifyConfigMapper notifyConfigMapper;
	
	@Override
	public String getToken() {
		return "mailserver/save";
	}

	@Override
	public String getName() {
		return "nmtam.mailserversaveapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "uuid", type = ApiParamType.STRING, desc = "common.uuid"),
			@Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, isRequired= true, maxLength = 50, desc = "common.name"),
			@Param(name = "host", type = ApiParamType.STRING, isRequired = true,  maxLength = 50, desc = "term.framework.smpthost"),
			@Param(name = "port", type = ApiParamType.INTEGER, isRequired = true, desc = "term.framework.smptport"),
			@Param(name = "userName", type = ApiParamType.STRING, isRequired = true,  maxLength = 50, desc = "common.username"),
			@Param(name = "password", type = ApiParamType.STRING, isRequired = true,  maxLength = 50, desc = "common.password"),
			@Param(name = "domain", type = ApiParamType.STRING,  maxLength = 50, desc = "term.framework.domain"),
			@Param(name = "fromAddress", type = ApiParamType.STRING, isRequired = true,  maxLength = 50, desc = "common.mailaddress"),
			@Param(name = "sslEnable", type = ApiParamType.ENUM, rule = "true,false", isRequired = true, maxLength = 50, desc = "term.framework.smptsslenable")
	})
	@Output({})
	@Description(desc = "nmtam.mailserversaveapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		MailServerVo mailServerVo = jsonObj.toJavaObject(MailServerVo.class);
		notifyConfigMapper.insertNotifyConfig(NotifyHandlerType.EMAIL.getValue(), JSONObject.toJSONString(mailServerVo));
		return null;
	}

}
