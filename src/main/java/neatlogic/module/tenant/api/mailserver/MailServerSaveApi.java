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
import neatlogic.framework.dao.mapper.MailServerMapper;
import neatlogic.framework.dto.MailServerVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import neatlogic.module.tenant.exception.mailserver.MailServerNameRepeatException;
import neatlogic.module.tenant.exception.mailserver.MailServerNotFoundException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional
@AuthAction(action = NOTIFY_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class MailServerSaveApi extends PrivateApiComponentBase {

	@Autowired
	private MailServerMapper mailServerMapper;
	
	@Override
	public String getToken() {
		return "mailserver/save";
	}

	@Override
	public String getName() {
		return "邮件服务器信息保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "uuid"),
		@Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, isRequired= true, maxLength = 50, desc = "名称"),
		@Param(name = "host", type = ApiParamType.STRING, isRequired = true,  maxLength = 50, desc = "smtp主机"),
		@Param(name = "port", type = ApiParamType.INTEGER, isRequired = true, desc = "smtp端口"),
		@Param(name = "userName", type = ApiParamType.STRING, isRequired = true,  maxLength = 50, desc = "用户"),
		@Param(name = "password", type = ApiParamType.STRING, isRequired = true,  maxLength = 50, desc = "密码"),
		@Param(name = "domain", type = ApiParamType.STRING,  maxLength = 50, desc = "域名"),
		@Param(name = "fromAddress", type = ApiParamType.STRING, isRequired = true,  maxLength = 50, desc = "")
	})
	@Output({
		@Param(name = "Return", type = ApiParamType.STRING, desc = "邮件服务器uuid")
	})
	@Description(desc = "邮件服务器信息保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		MailServerVo mailServerVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<MailServerVo>() {});
		if(mailServerMapper.checkMailServerNameIsRepeat(mailServerVo) > 0) {
			throw new MailServerNameRepeatException(mailServerVo.getName());
		}
		
		String uuid = jsonObj.getString("uuid");
		if(uuid != null) {
			if(mailServerMapper.checkMailServerIsExists(uuid) == 0) {
				throw new MailServerNotFoundException(uuid);
			}
		}
		mailServerMapper.replaceMailServer(mailServerVo);		
		return mailServerVo.getUuid();
	}

}
